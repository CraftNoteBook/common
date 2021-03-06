package com.mengcraft.util;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created on 17-1-18.
 */
public class PluginHelper {

    public static class Runner extends BukkitRunnable implements ICancellable {

        private final IRunner r;

        Runner(IRunner r) {
            this.r = r;
        }

        @Override
        public void run() {
            r.run(this);
        }
    }

    public static class Exec extends Command {

        private final IExec exec;

        Exec(String name, IExec exec) {
            super(name);
            this.exec = exec;
        }

        @Override
        public boolean execute(CommandSender sender, String l, String[] i) {
            return testPermission(sender) && exec.exec(sender, l, ImmutableList.copyOf(i));
        }
    }

    public interface IExec {

        boolean exec(CommandSender sender, String command, List<String> list);
    }

    public interface ICancellable {

        void cancel();
    }

    public interface IRunner {

        void run(ICancellable r);
    }

    public static int run(Plugin plugin, int i, int repeat, IRunner r) {
        Runner out = new Runner(r);
        if (i > 0) {
            if (repeat > 0) {
                out.runTaskTimer(plugin, i, repeat);
            } else {
                out.runTaskLater(plugin, i);
            }
        } else {
            out.runTask(plugin);
        }
        return out.getTaskId();
    }

    public static void addExecutor(Plugin plugin, Command command) {
        try {
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);
            CommandMap map = (CommandMap) f.get(plugin.getServer().getPluginManager());
            map.register(plugin.getName().toLowerCase(), command);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static void addExecutor(Plugin plugin, String command, IExec exec) {
        addExecutor(plugin, command, null, exec);
    }

    public static void addExecutor(Plugin plugin, String command, String permission, IExec exec) {
        Exec e = new Exec(command, exec);
        e.setPermission(permission);
        e.setPermissionMessage(ChatColor.RED + "您没有权限执行此类指令，请联系管理！");
        addExecutor(plugin, e);
    }

}
