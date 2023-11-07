package com.hpplay.sdk.sink.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by duyifeng on 2017/11/24.
 * Device工具类
 */
public class Utils {

    private static final String TAG = "Utils";

    public static final int STREAM_READ_WRITE_DEFAULT_SIZE = 8 * 1024;

    public static String createDeviceName(Context context) {
        String ip = getIP(context);
        String deviceNmae = ip;
        String[] dump = ip.split("\\.");
        if (dump.length > 0) {
            deviceNmae = dump[dump.length - 1];
        }
        deviceNmae = "Lelink#" + deviceNmae;
        Logger.i(TAG, "createDeviceName: " + deviceNmae);
        return deviceNmae;
    }

    public static String getIP(Context context) {
        String ip = "";
        try {

            // 判断是否是有线网络
            boolean eth0 = false;
            boolean wifi = false;
            boolean mobile = false;
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase("Ethernet")) {
                    eth0 = true;
                } else if (type.equalsIgnoreCase("WIFI")) {
                    wifi = true;
                } else if (type.equalsIgnoreCase("MOBILE")) {
                    mobile = true;
                }
            }

            // 在有些设备上wifi和有线同时存在，获得的ip会有两个
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en == null) {
                return ip;
            }
            while (en.hasMoreElements()) {
                NetworkInterface element = en.nextElement();
                Enumeration<InetAddress> inetAddresses = element.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        ip = inetAddress.getHostAddress().toString();
                        Logger.i(TAG, "getIPAddress: " + ip);
                        if (eth0) {
                            if (element.getDisplayName().equals("eth0")) {
                                return ip;
                            }
                        } else if (wifi) {
                            if (element.getDisplayName().equals("wlan0")) {
                                return ip;
                            }
                        } else if (mobile) {
                            return ip;
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.w(TAG, ex);
        }
        return ip;
    }

    /* 获取WiFi的SSID */
    /* 判断当前网路有线还是WiFi */
    public static String getNetWorkName(Context context) {

        String wired_network = "有线网络";
        String wireless_network = "无线网络";
        String mobile_network = "移动网络";
        String net_error = "网络错误";

        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase("Ethernet")) {
                    return wired_network;
                } else if (type.equalsIgnoreCase("WIFI")) {
                    String tmpssid = getWifiSSID(context);

                    if (tmpssid.contains("unknown") || tmpssid.contains("0x")) {
                        tmpssid = wireless_network;
                    }
                    return tmpssid;
                } else if (type.equalsIgnoreCase("MOBILE")) {
                    return mobile_network;
                } else {
                    return wired_network;
                }
            } else {
                String apName = getAPName(context);
                if (!TextUtils.isEmpty(apName)) {
                    return apName;
                }
                return net_error;
            }
        } catch (Exception e) {
            Logger.w(TAG, e);
            return net_error;
        }
    }

    private static String getAPName(Context context) {
        if (!isWifiApOpen(context)) {
            return "";
        }
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(manager);
            return configuration.SSID;
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
        return "";
    }

    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            int state = (int) method.invoke(manager);
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            int value = (int) field.get(manager);
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
        return false;
    }

    private static String getWifiSSID(Context context) {
        String ssid = "unknown id";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return info.getSSID();
            } else {
                return info.getSSID().replace("\"", "");
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo() != null) {
                    return networkInfo.getExtraInfo().replace("\"", "");
                }
            }
        }
        return ssid;
    }

    /**
     * @return
     */
    public static Bitmap createQRCode(String qrUrl, int size, int padding) {
        Bitmap bitmapQR = null;
        try {
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix matrix = new QRCodeWriter().encode(qrUrl,
                    BarcodeFormat.QR_CODE, size, size, hints);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            //
            boolean isFirstBlackPoint = false;
            int startX = 0;
            int startY = 0;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (matrix.get(x, y)) {
                        if (isFirstBlackPoint == false) {
                            isFirstBlackPoint = true;
                            startX = x;
                            startY = y;
                        }
                        pixels[y * size + x] = 0xff000000;
                    } else {
                        pixels[y * size + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);

            // 剪切中间的二维码区域，减少padding区域
            if (startX <= padding) {
                return bitmap;
            }

            int x1 = startX - padding;
            int y1 = startY - padding;
            if (x1 < 0 || y1 < 0) {
                return bitmap;
            }

            int w1 = width - x1 * 2;
            int h1 = height - y1 * 2;

            bitmapQR = Bitmap.createBitmap(bitmap, x1, y1, w1, h1);

        } catch (Exception e) {
            Logger.w(TAG, e);
        }
        return bitmapQR;
    }

    public static int getScreenWidth(Context context) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int HOUR = 1000 * 60 * 60;
    public static int MINUTE = 1000 * 60;
    public static int SECOND = 1000;

    public static String getVideoTimeStr(long timeStamp) {

        if (timeStamp > 0) {

            int hour = (int) timeStamp / HOUR;
            int minute = (int) timeStamp % HOUR / MINUTE;
            int second = (int) timeStamp % HOUR % MINUTE / SECOND;

            String hourStr = String.valueOf(hour);
            if (hour > 0 && hour < 10) {
                hourStr = "0" + hourStr;
            }
            String minuteStr = String.valueOf(minute);
            if (minute < 10) {
                minuteStr = "0" + minuteStr;
            }

            String secondStr = String.valueOf(second);
            if (second < 10) {
                secondStr = "0" + secondStr;
            }

            if (hour > 0) {
                return hourStr + ":" + minuteStr + ":" + secondStr;
            } else
                return minuteStr + ":" + secondStr;

        }

        return "00:00";

    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }

    // 用缓冲流复制文件函数
    public static void copyFile(File sourceFile, File targetFile) {
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            // 新建文件输入流并对它进行缓冲
            input = new FileInputStream(sourceFile);
            // 新建文件输出流并对它进行缓冲
            output = new FileOutputStream(targetFile);
            int offset;
            //改为系统默认的8k,而不是1k 在部分老设备上面解压速度有明显的提升
            byte[] buffer = new byte[STREAM_READ_WRITE_DEFAULT_SIZE];
            while ((offset = input.read(buffer, 0, STREAM_READ_WRITE_DEFAULT_SIZE)) != -1) {
                output.write(buffer, 0, offset);
            }
            // 刷新此缓冲的输出流
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Logger.w(TAG, e);
        } finally {
            // 关闭流
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                Logger.w(TAG, e);
            }
        }
    }
}
