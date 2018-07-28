package com.example.radu5.turistgroupchat;

import com.example.radu5.turistgroupchat.Model.ChatGroup;
import com.example.radu5.turistgroupchat.Model.ListItem;

import java.util.ArrayList;

/**
 * Created by radu5 on 5/4/2018.
 */

public interface IMainActivity {
    public void onListItemLongClicked(int listId,String listName);
    public void onListSelected(ArrayList<ListItem> list);
    public void onChatGroupSelected(int groupid,String groupName);
}
