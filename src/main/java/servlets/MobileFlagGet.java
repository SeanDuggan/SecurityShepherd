package servlets;

import dbProcs.Getter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.Hash;
import utils.ShepherdLogManager;

/**
 * Returns a user-specific dynamic flag for the requested mobile module. <br>
 * <br>
 * The flag is the HMAC-SHA512 of the module's base flag keyed with the server's ephemeral key and
 * salted with the authenticated user's name — matching exactly what {@link MobileFlagSubmit} will
 * recompute during validation. This means:
 *
 * <ul>
 *   <li>Each student receives a different flag string.
 *   <li>Flags cannot be shared between students.
 *   <li>The base flag value is never sent to the client.
 * </ul>
 *
 * <p>Request (POST, application/x-www-form-urlencoded):
 *
 * <pre>
 *   login    – Shepherd username
 *   pwd      – Shepherd password
 *   moduleId – Mobile module identifier (e.g. "client_side_injection_lesson")
 * </pre>
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"flag":"&lt;hmac-hex&gt;"}                  – authenticated, module known
 *   {"error":"..."}                          – authentication failure or unknown module
 * </pre>
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */
public class MobileFlagGet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileFlagGet.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(
        request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileFlagGet ****");

    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    String login = request.getParameter("login");
    String pwd = request.getParameter("pwd");
    String moduleId = request.getParameter("moduleId");

    if (login == null || pwd == null || moduleId == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      out.write(errorJson("Missing required parameters"));
      return;
    }

    String applicationRoot = getServletContext().getRealPath("");
    String[] user;
    try {
      user = Getter.authUser(applicationRoot, login, pwd);
    } catch (Exception e) {
      log.error("Authentication error: " + e.toString());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      out.write(errorJson("Server error"));
      return;
    }

    if (user == null || user[0] == null || user[0].isEmpty()) {
      log.debug("Authentication failed for: " + login);
      try {
        Thread.sleep(2000);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      out.write(errorJson("Invalid credentials"));
      return;
    }

    String baseFlag = MobileModuleFlags.BASE_FLAGS.get(moduleId);
    if (baseFlag == null) {
      log.debug("Unknown mobile module ID: " + moduleId);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.write(errorJson("Module not found"));
      return;
    }

    // Derive a user-specific flag. Hash.generateUserSolutionKeyOnly uses HmacSHA512
    // keyed with the server's ephemeral key — same computation as MobileFlagSubmit.
    String userName = user[1];
    String dynamicFlag = Hash.generateUserSolutionKeyOnly(baseFlag, userName);
    log.debug("Returning dynamic flag for " + moduleId + " to " + userName);

    JSONObject result = new JSONObject();
    result.put("flag", dynamicFlag);
    out.write(result.toString());
    log.debug("**** END MobileFlagGet ****");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private static String errorJson(String message) {
    JSONObject obj = new JSONObject();
    obj.put("error", message);
    return obj.toString();
  }
}
