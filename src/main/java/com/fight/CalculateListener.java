package com.fight;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

// 按两次 Shift 打开“随处搜索”对话框并输入 `show whitespaces`，
// 然后按 Enter 键。现在，您可以在代码中看到空格字符。
@Log4j2
public class CalculateListener implements ClipboardOwner {
    private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();


    public CalculateListener() {
        // 将剪切板的所有者设置为自己
        // 当所有者为自己时，才能监控下一次剪切板的变动
        // clipboard.getContents(null) 获取当前剪切板的内容
        clipboard.setContents(clipboard.getContents(null), this);
    }


    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // 延迟1s执行，如果立即执行会报错，系统还没使用完剪切板，直接操作会报错
        // IllegalStateException: cannot open system clipboard
        synchronized (this) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
               log.error(e);
            }
            String text = null;
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                try {
                    // 获取文本数据
                    Transferable transferable = clipboard.getContents(null);
                    text = ((String) transferable.getTransferData(DataFlavor.stringFlavor)).trim();
                    String remove = StringUtils.replaceOnce(text, ".", "");
                    if (StringUtils.isNumeric(remove) && StringUtils.contains(text, ".")) {
                        String string = getFinalStr(text);
                        clipboard.setContents(new StringSelection(string), null);
                        Robot robot = new Robot();
                        // 按下Ctrl+V键
                        robot.keyPress(java.awt.event.KeyEvent.VK_CONTROL);
                        robot.keyPress(KeyEvent.VK_V);
                        robot.keyRelease(java.awt.event.KeyEvent.VK_CONTROL);
                        robot.keyRelease(java.awt.event.KeyEvent.VK_V);

                        Thread.sleep(100); // 等待粘贴操作完成
                    }
                } catch (UnsupportedFlavorException | IOException e) {
                    log.error(e);
                } catch (AWTException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // 不影响剪切板内容
        // 每次剪切板变动，剪切板的所有者会被剥夺，所以要重新设置自己为所有者，才能监听下一次剪切板变动
        clipboard.setContents(clipboard.getContents(null), this);
    }

    private static String getFinalStr(String text) {
        try {
            BigDecimal bigDecimal = new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
            BigDecimal divide;
            //数字超过400不转化,即成本超过400不卖
            if (bigDecimal.compareTo(new BigDecimal("400")) < 0) {
                if (bigDecimal.longValue() > 100) {
                    divide = bigDecimal.add(BigDecimal.valueOf(110)).divide(new BigDecimal("0.7"), RoundingMode.HALF_DOWN);
                } else {
                    divide = bigDecimal.multiply(BigDecimal.valueOf(3L)).add(new BigDecimal("8.6"));
                }
                return divide.setScale(1, RoundingMode.HALF_UP).toString();
            } else {
                return text;
            }
        } catch (Exception e) {
            return text;
        }

    }
}
