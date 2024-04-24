package com.fight;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// 按两次 Shift 打开“随处搜索”对话框并输入 `show whitespaces`，
// 然后按 Enter 键。现在，您可以在代码中看到空格字符。
@Log4j2
public class ClipboardListener implements ClipboardOwner {
    private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private Map<String, Byte> map = readExistFile("选品.xlsx");


    public ClipboardListener() {
        // 将剪切板的所有者设置为自己
        // 当所有者为自己时，才能监控下一次剪切板的变动
        // clipboard.getContents(null) 获取当前剪切板的内容
        clipboard.setContents(clipboard.getContents(null), this);
    }


    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // 延迟1s执行，如果立即执行会报错，系统还没使用完剪切板，直接操作会报错
        // IllegalStateException: cannot open system clipboard
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
                text = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                String code = StringUtils.substringBetween(text, "offer/", ".html");
                if (map.get(code) == null && !StringUtils.isAllBlank(code)) {
                    String fileName = "选品.xlsx";
                    writeNewUrl(text, fileName);
                    map.put(code, (byte) 1);
                    log.info("写入" + text);
                } else {
                    log.info("重复或非法,不写入" + text);
                }
            } catch (UnsupportedFlavorException e) {
                log.error(e);
            } catch (IOException e) {
                log.error(e);
            }
        }


        // 不影响剪切板内容
        // 每次剪切板变动，剪切板的所有者会被剥夺，所以要重新设置自己为所有者，才能监听下一次剪切板变动
        clipboard.setContents(clipboard.getContents(null), this);
    }

    public static Map<String, Byte> readExistFile(String filePath) {
        Map<String, Byte> map = new HashMap<>();
        try (FileInputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
            for (Row row : sheet) { // 迭代行
                Cell second = row.getCell(1);
                // 获取第二列单元格数据，这里简单处理为统一使用字符串格式
                String cellValue = getCellValueAsString(second);
                map.put(StringUtils.substringBetween(cellValue, "offer/", ".html"), (byte) 1);
            }
        } catch (IOException ioe) {
            log.error("选品>文件不存在");
        }
        return map;
    }

    public static void writeNewUrl(String content, String fileName) {
        if (!FileUtils.getFile(fileName).exists()) {
            try (FileOutputStream os = new FileOutputStream(fileName);
                 SXSSFWorkbook wb = new SXSSFWorkbook()) {
                SXSSFSheet sheet = wb.createSheet();
                wb.write(os);
                log.info("创建sheet");
            } catch (IOException e) {
                log.error("创建文件失败");
                throw new RuntimeException(e);
            }
        }
        try (FileInputStream fileInputStream = new FileInputStream(fileName);
             Workbook workbook = new XSSFWorkbook(fileInputStream);
             FileOutputStream os = new FileOutputStream(fileName)
        ) {
            Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
            int lastRowNum = sheet.getLastRowNum();

            Row row = sheet.createRow(lastRowNum + 1);
            Cell cell = row.createCell(1, CellType.STRING);
            cell.setCellValue(content);
            workbook.write(os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> String.valueOf(cell.getCellFormula());
            default -> "";
        };
    }
}
