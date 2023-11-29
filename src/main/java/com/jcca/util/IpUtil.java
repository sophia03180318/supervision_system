package com.jcca.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class IpUtil {

    private static Pattern IPPATTERN = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");

    /**
     * 获取登录用户IP地址
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.equals("0:0:0:0:0:0:0:1")) {
            ip = "本地";
        }
        return ip;
    }


    // 获取本地局域网ip地址
    public static String getMyIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                // 过滤回环、虚拟、断开的网卡
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }

                // 过滤虚拟机网卡
                if (netInterface.getDisplayName().contains("Virtual")) {
                    continue;
                }

                // 过滤VPN
                if (netInterface.getDisplayName().contains("VPN")) {
                    continue;
                }


                // 获取真实可用的网卡
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) { // 必须是IPv4
                        //过滤169私有地址
                        if (ip.getHostAddress().startsWith("169")) {
                            break;
                        }
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取本地IP地址失败", e);
        }
        return "";
    }

    // 获取本地局域网mac地址
    public static String getMyMacAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            byte[] mac = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                // 过滤回环、虚拟、断开的网卡
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }

                // 过滤虚拟机网卡
                if (netInterface.getDisplayName().contains("Virtual")) {
                    continue;
                }

                // 获取真实可用的网卡
                mac = netInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    if (sb.length() > 0) {
                        return sb.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取本地MAC地址获取失败", e);
        }
        return "";
    }

    //净化ip,从ip列表中,排除自己以前的垃圾ip
    public static List<String> purifyIp(String currIp, List<String> urllist) {
        List<String> myIpList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                // 获取真实可用的网卡
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) { // 必须是IPv4
                        myIpList.add(ip.getHostAddress());
                    }
                }

            }
        } catch (Exception e) {
            log.error("获取本地IP列表失败", e);
        }

        //iplist中含有myIpList的ip并且不等于currIp,需要排除
        //垃圾ip列表
        List<String> garbage = new ArrayList<>();

        for (int j = 0; j < myIpList.size(); j++) {
            if (existIpByurlList(myIpList.get(j), urllist) && !myIpList.get(j).equals(currIp)) {
                garbage.add(myIpList.get(j));
            }
        }

        urllist.removeIf(e -> {
            boolean del = false;
            for (int i = 0; i < garbage.size(); i++) {
                if (e.startsWith(garbage.get(i))) {
                    del = true;
                    break;
                }
            }
            return del;
        });

        return urllist;
    }

    ;

    /**
     * 检测某个ip是否在url(ip+端口)中存在
     *
     * @return
     */
    public static boolean existIpByurlList(String ip, List<String> urlList) {
        for (int i = 0; i < urlList.size(); i++) {
            if (urlList.get(i).startsWith(ip)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 正则判断是否IP
     *
     * @param ip
     * @return
     */
    public static Boolean isIp(String ip) {
        Matcher matcher = IPPATTERN.matcher(ip);
        return matcher.find();
    }

}
