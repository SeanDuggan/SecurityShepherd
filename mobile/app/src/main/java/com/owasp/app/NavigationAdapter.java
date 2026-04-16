package com.owasp.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {

    private List<NavigationItem> items;
    private List<NavigationItem> displayedItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NavigationItem item);
    }

    public NavigationAdapter(List<NavigationItem> items, OnItemClickListener listener) {
        this.items = items;
        this.displayedItems = new ArrayList<>();
        this.listener = listener;
        updateDisplayedItems();
    }

    private void updateDisplayedItems() {
        displayedItems.clear();
        for (NavigationItem item : items) {
            addItemWithChildren(item, 0);
        }
    }
    
    private void addItemWithChildren(NavigationItem item, int depth) {
        displayedItems.add(item);
        if (item.isExpandable() && item.isExpanded()) {
            for (NavigationItem child : item.getChildren()) {
                addItemWithChildren(child, depth + 1);
            }
        }
    }
    
    private int getItemDepth(NavigationItem item) {
        return getItemDepth(item, items, 0);
    }
    
    private int getItemDepth(NavigationItem target, List<NavigationItem> itemList, int currentDepth) {
        for (NavigationItem item : itemList) {
            if (item == target) {
                return currentDepth;
            }
            if (item.isExpandable()) {
                int childDepth = getItemDepth(target, item.getChildren(), currentDepth + 1);
                if (childDepth != -1) {
                    return childDepth;
                }
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nav_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NavigationItem item = displayedItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView title;
        private ImageView expandIcon;
        private View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            icon = itemView.findViewById(R.id.nav_item_icon);
            title = itemView.findViewById(R.id.nav_item_title);
            expandIcon = itemView.findViewById(R.id.nav_item_expand_icon);
        }

        public void bind(NavigationItem item) {
            title.setText(item.getTitle());
            
            int depth = getItemDepth(item);
            int leftPadding = 16 + (depth * 32); // 16dp base, 32dp per level
            
            itemView.setPaddingRelative(
                leftPadding,
                itemView.getPaddingTop(),
                itemView.getPaddingEnd(),
                itemView.getPaddingBottom()
            );
            
            // Show icon only for top-level items (depth 0)
            if (depth == 0 && item.getIconResId() != 0) {
                icon.setImageResource(item.getIconResId());
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }
            
            // Show expand icon for expandable items at any level
            if (item.isExpandable()) {
                expandIcon.setVisibility(View.VISIBLE);
                expandIcon.setRotation(item.isExpanded() ? 180 : 0);
            } else {
                expandIcon.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (item.isExpandable()) {
                    item.setExpanded(!item.isExpanded());
                    updateDisplayedItems();
                    notifyDataSetChanged();
                } else {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
