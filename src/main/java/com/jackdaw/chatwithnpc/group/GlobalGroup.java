package com.jackdaw.chatwithnpc.group;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class GlobalGroup extends LocalGroup implements Group {
    GlobalGroup() {
        super("Global");
    }

    /**
     * 获取全局环境的实例
     * @return 全局环境的实例
     */
    @Contract(" -> new")
    public static @NotNull GlobalGroup getGlobalGroup(){
        GlobalGroup globalGroup = (GlobalGroup) GroupManager.getGroup("Global");
        if (globalGroup == null){
            GroupManager.loadEnvironment("Global");
            globalGroup = (GlobalGroup) GroupManager.getGroup("Global");
        }
        return globalGroup;
    }
}
