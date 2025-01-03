package org.message.SLEMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SLEMessage extends JavaPlugin implements Listener, TabCompleter {

    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // 플러그인 활성화 시 설정 파일 로드 또는 생성
        configFile = new File(getDataFolder(), "setmessage.yml");
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);

                // 기본 메시지 설정
                config.set("joinMessage", "[서버] 환영합니다, {player}!");
                config.set("quitMessage", "[서버] {player}님이 퇴장하셨습니다.");
                saveConfigFile();

                // 파일 생성 알림 메시지 출력
                getLogger().info(ChatColor.GREEN + "[ SLEMessage ] 설정 파일 'setmessage.yml'이 생성되었습니다.");
            } catch (IOException e) {
                getLogger().severe("설정 파일을 생성하는 중 오류가 발생했습니다.");
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("메세지설정").setTabCompleter(this);

        getLogger().info("[ SLEMessage ] 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        // 플러그인 비활성화 시 설정 저장
        saveConfigFile();
        getLogger().info("[ SLEMessage ] 플러그인이 비활성화되었습니다.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String rawMessage = config.getString("joinMessage");
        String message = ChatColor.translateAlternateColorCodes('&', rawMessage.replace("{player}", player.getName()));
        event.setJoinMessage(message);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String rawMessage = config.getString("quitMessage");
        String message = ChatColor.translateAlternateColorCodes('&', rawMessage.replace("{player}", player.getName()));
        event.setQuitMessage(message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("메세지설정")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.YELLOW + "=====================" + ChatColor.GOLD + " [ 도움말 ] " + ChatColor.YELLOW + "=====================");
                sender.sendMessage(ChatColor.AQUA + " ▶ " + ChatColor.GREEN + "/메세지설정 입장 [메세지]" + ChatColor.WHITE + " - 입장 메시지를 설정합니다.");
                sender.sendMessage(ChatColor.AQUA + " ▶ " + ChatColor.GREEN + "/메세지설정 퇴장 [메세지]" + ChatColor.WHITE + " - 퇴장 메시지를 설정합니다.");
                sender.sendMessage(ChatColor.AQUA + " ▶ " + ChatColor.GREEN + "/메세지설정 확인" + ChatColor.WHITE + " - 설정한 입장, 퇴장 메세지를 확인합니다.");
                sender.sendMessage(ChatColor.WHITE + "플레이어 닉네임은 {player} 이걸로 입력해주어야 합니다.");
                sender.sendMessage(ChatColor.YELLOW + "====================================================");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("확인")) {
                String joinMessage = config.getString("joinMessage", "입장 메시지가 설정되지 않았습니다.");
                String quitMessage = config.getString("quitMessage", "퇴장 메시지가 설정되지 않았습니다.");

                sender.sendMessage(ChatColor.YELLOW + "=====================" + ChatColor.GOLD + " [ 설정된 메시지 ] " + ChatColor.YELLOW + "=====================");
                sender.sendMessage(ChatColor.GREEN + "✔ 현재 입장 메시지: " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', joinMessage));
                sender.sendMessage(ChatColor.GREEN + "✔ 현재 퇴장 메시지: " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', quitMessage));
                sender.sendMessage(ChatColor.YELLOW + "====================================================");
                return true;
            }

            if (args.length >= 2) {
                String type = args[0].toLowerCase();
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                if (type.equals("입장")) {
                    config.set("joinMessage", message);
                    saveConfigFile();
                    sender.sendMessage(ChatColor.GREEN + "✔ 입장 메시지가 설정되었습니다: " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
                    return true;
                } else if (type.equals("퇴장")) {
                    config.set("quitMessage", message);
                    saveConfigFile();
                    sender.sendMessage(ChatColor.GREEN + "✔ 퇴장 메시지가 설정되었습니다: " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "✘ 올바르지 않은 명령어입니다. /메세지설정을 입력하여 도움말을 확인하세요.");
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("메세지설정")) {
            List<String> completions = new ArrayList<>();

            if (args.length == 1) {
                completions.add("입장");
                completions.add("퇴장");
            }
            else if (args.length == 2 && args[0].equalsIgnoreCase("입장")) completions.add("[ 원하는 입장 메세지 ]");
            else if (args.length == 2 && args[0].equalsIgnoreCase("퇴장")) completions.add("[ 원하는 퇴장 메세지 ]");

            return completions;
        }

        return null;
    }

    private void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            getLogger().severe("설정 파일을 저장하는 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
}
