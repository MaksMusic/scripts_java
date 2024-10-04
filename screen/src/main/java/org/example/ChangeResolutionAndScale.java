package org.example;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class ChangeResolutionAndScale {

    // Интерфейс для работы с User32.dll
    public interface User32Ext extends StdCallLibrary {
        User32Ext INSTANCE = Native.load("user32", User32Ext.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean EnumDisplaySettingsW(String lpszDeviceName, int iModeNum, DEVMODE lpDevMode);
        int ChangeDisplaySettingsExW(String lpszDeviceName, DEVMODE lpDevMode, int hwnd, int dwFlags, int lParam);
        boolean SystemParametersInfo(int uiAction, int uiParam, String pvParam, int fWinIni);
    }

    // Константы для SystemParametersInfo
    public static final int SPI_SETNONCLIENTMETRICS = 0x002A;
    public static final int SPIF_UPDATEINIFILE = 0x01;
    public static final int SPIF_SENDCHANGE = 0x02;

    // Изменение разрешения экрана
    public static void changeResolution(int width, int height) {
        User32Ext user32 = User32Ext.INSTANCE;
        DEVMODE dm = new DEVMODE();
        dm.dmSize = (short) dm.size();

        if (user32.EnumDisplaySettingsW(null, ENUM_CURRENT_SETTINGS, dm)) {
            dm.dmPelsWidth = width;
            dm.dmPelsHeight = height;
            dm.dmFields = DEVMODE.DM_PELSWIDTH | DEVMODE.DM_PELSHEIGHT;
            int result = user32.ChangeDisplaySettingsExW(null, dm, 0, 0, 0);

            if (result == DISP_CHANGE_SUCCESSFUL) {
                System.out.println("Resolution changed to " + width + "x" + height);
            } else {
                System.out.println("Failed to change resolution, error code: " + result);
            }
        } else {
            System.out.println("Unable to get current display settings.");
        }
    }

    // Изменение масштаба экрана (DPI)
    public static void changeScale(int scalePercentage) {
        // Путь к ключу реестра для изменения масштаба
        String registryPath = "Control Panel\\Desktop";
        String valueName = "LogPixels";

        // Рассчитываем значение DPI для масштаба
        int dpiValue = (int) (96 * (scalePercentage / 100.0));

        // Изменяем значение LogPixels в реестре
        Advapi32Util.registrySetIntValue(WinReg.HKEY_CURRENT_USER, registryPath, valueName, dpiValue);

        System.out.println("Scale set to " + scalePercentage + "%");

        // Перезагружаем рабочий стол для применения изменений
        User32Ext.INSTANCE.SystemParametersInfo(SPI_SETNONCLIENTMETRICS, 0, null, SPIF_UPDATEINIFILE | SPIF_SENDCHANGE);
    }

    public static void main(String[] args) {
        // Устанавливаем разрешение 1920x1080
        changeResolution(1920, 1080);

        // Устанавливаем масштаб 125%
        changeScale(125);
    }

    // Константы для ChangeDisplaySettingsExW
    public static final int ENUM_CURRENT_SETTINGS = -1;
    public static final int DISP_CHANGE_SUCCESSFUL = 0;

    // Структура DEVMODE
    public static class DEVMODE extends com.sun.jna.Structure {
        public static final int DM_PELSWIDTH = 0x00080000;
        public static final int DM_PELSHEIGHT = 0x00100000;

        public char[] dmDeviceName = new char[32];
        public short dmSpecVersion;
        public short dmDriverVersion;
        public short dmSize;
        public short dmDriverExtra;
        public int dmFields;
        public int dmPositionX;
        public int dmPositionY;
        public int dmDisplayOrientation;
        public int dmDisplayFixedOutput;
        public short dmColor;
        public short dmDuplex;
        public short dmYResolution;
        public short dmTTOption;
        public short dmCollate;
        public char[] dmFormName = new char[32];
        public short dmLogPixels;
        public int dmBitsPerPel;
        public int dmPelsWidth;
        public int dmPelsHeight;
        public int dmDisplayFlags;
        public int dmDisplayFrequency;
        public int dmICMMethod;
        public int dmICMIntent;
        public int dmMediaType;
        public int dmDitherType;
        public int dmReserved1;
        public int dmReserved2;
        public int dmPanningWidth;
        public int dmPanningHeight;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("dmDeviceName", "dmSpecVersion", "dmDriverVersion", "dmSize", "dmDriverExtra",
                    "dmFields", "dmPositionX", "dmPositionY", "dmDisplayOrientation", "dmDisplayFixedOutput",
                    "dmColor", "dmDuplex", "dmYResolution", "dmTTOption", "dmCollate", "dmFormName", "dmLogPixels",
                    "dmBitsPerPel", "dmPelsWidth", "dmPelsHeight", "dmDisplayFlags", "dmDisplayFrequency",
                    "dmICMMethod", "dmICMIntent", "dmMediaType", "dmDitherType", "dmReserved1", "dmReserved2",
                    "dmPanningWidth", "dmPanningHeight");
        }
    }
}
