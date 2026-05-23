package servlets;

import dbProcs.Getter;
import dbProcs.Setter;
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
 * Stateless flag-validation endpoint for the Security Shepherd mobile app.
 *
 * <p>Validates the flag the student discovered inside a challenge by recomputing the same
 * user-specific HMAC that {@link MobileFlagGet} produced. Because validation is HMAC-based rather
 * than a static hash comparison, each student has a unique flag and flags cannot be shared or
 * precomputed from the APK.
 *
 * <p>Request (POST, application/x-www-form-urlencoded):
 *
 * <pre>
 *   login    - Shepherd username
 *   pwd      - Shepherd password
 *   moduleId - Module identifier string (e.g. "client_side_injection_lesson")
 *   flag     - Flag string discovered by the student
 * </pre>
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"correct":true}
 *   {"correct":false,"message":"..."}
 * </pre>
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see http://www.gnu.org/licenses/.
 *
 * @author Sean Duggan
 */
public class MobileFlagSubmit extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileFlagSubmit.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(
        request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileFlagSubmit ****");

    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    String login = request.getParameter("login");
    String pwd = request.getParameter("pwd");
    String moduleId = request.getParameter("moduleId");
    String flag = request.getParameter("flag");

    if (login == null || pwd == null || moduleId == null || flag == null) {
      log.debug("Missing required parameters");
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

    // Recompute the same HMAC that MobileFlagGet produced for this user.
    // Hash.generateUserSolutionKeyOnly uses HmacSHA512 keyed with the server`s
    // ephemeral key, which is constant for the lifetime of the server process.
    String userName = user[1];
    String expectedFlag = Hash.generateUserSolutionKeyOnly(baseFlag, userName);
    boolean correct = expectedFlag != null && expectedFlag.equalsIgnoreCase(flag.trim());

    log.debug(
        "Flag submission for "
            + moduleId
            + " by "
            + userName
            + ": "
            + (correct ? "correct" : "incorrect"));

    if (correct) {
      // Record completion and award points if this module has a DB entry
      String dbModuleId = MobileModuleFlags.MODULE_DB_IDS.get(moduleId);
      if (dbModuleId != null) {
        String userId = user[0];
        try {
          // Mobile users never go through GetModule, so there may be no results row.
          // Calling getModuleAddress triggers moduleGetHash which inserts one if missing.
          Getter.getModuleAddress(applicationRoot, dbModuleId, userId);
          // checkPlayerResult returns the module name when in-progress (not yet completed).
          // Returns null when already completed or when there was no row (both safe to skip).
          String inProgress = Getter.checkPlayerResult(applicationRoot, dbModuleId, userId);
          if (inProgress != null) {
            Setter.updatePlayerResult(
                applicationRoot, dbModuleId, userId, "Mobile App Submission", 1, 1, 1);
            log.debug("Score recorded for " + moduleId + " (" + dbModuleId + ") by " + userName);
          } else {
            log.debug(
                "Module " + moduleId + " already completed by " + userName + ", no score change");
          }
        } catch (Exception e) {
          // Scoring failure should not fail the flag validation response
          log.error("Failed to record score for " + moduleId + ": " + e.toString());
        }
      }
    }

    JSONObject result = new JSONObject();
    result.put("correct", correct);
    out.write(result.toString());
    log.debug("**** END MobileFlagSubmit ****");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private static String errorJson(String message) {
    JSONObject obj = new JSONObject();
    obj.put("correct", false);
    obj.put("message", message);
    return obj.toString();
  }
}