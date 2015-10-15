package com.jeiel.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
/**
 * ���Ȳ���ͳ��2014��ȣ��ϲ��ھ��µĺϲ����񱨱��ϲ�������ϲ��ֽ��������ϲ��ھ���Ҫ����ָ��4�����е�����ָ�꣺
    �ʲ��ܼơ�������Ȩ��ϼơ���ծ�ϼơ��ʲ���ծ�ʡ�������ĸ��˾�����ߵľ����󡢾�Ӫ��������ֽ����������Ϣ���ϱ����������ת��...
 * @author Administrator
 *
 */

public class TableExtractor {
	
	private static final String ROOT_DIR_PATH = "C:\\Users\\Administrator\\Desktop\\С��ļ";//��ǰĿ¼��Ϊ��.������
	private static final String DOC2DOCX_PATH = "./Doc2Docx.exe";
	private static final int TYPE_DOC = 0;
	private static final int TYPE_DOCX = 1;
	private static HSSFWorkbook book;
	private static HSSFSheet sheet;
	private static HSSFRow row;
	private static int rowIndex;
	
	public static void main(String[] args){
		List<File> fileList = initFileList();
		initExcelWriter();
		for(File file: fileList){
			row = sheet.createRow(rowIndex);
			chooseMethod(file);
			rowIndex++;
		}
		exportExcel();
		
	}
	
	public static List<File> initFileList(){
		List<File> fileList = new ArrayList<File>();
		File dir = new File(ROOT_DIR_PATH);
		System.out.println(dir.getAbsolutePath());
		if(dir.listFiles() != null){
			for(File file : dir.listFiles()){
				if(file.isFile() && checkFileType(file.getName())){
					fileList.add(file);
					System.out.println(file.getName());
				}
			}
		}
		return fileList;
	}
	
	public static boolean checkFileType(String fileName){
		if(fileName.toLowerCase().endsWith(".doc") ||
				fileName.toLowerCase().endsWith("docx")){
			return true;
		}
		return false;
	}
	
	public static void chooseMethod(File file){
		int type = -1;
		if(file.getName().toLowerCase().endsWith(".doc")){
			type = TYPE_DOC;
		}else if(file.getName().toLowerCase().endsWith(".docx")){
			type = TYPE_DOCX;
		}
		switch(type){
			case TYPE_DOC:
				extractFromDoc(file);
				break;
			case TYPE_DOCX:
				extractFromDocx(file);
				break;
			default:
				break;
		}
	}
	
	private static Process process = null;
	
	public static void extractFromDoc(final File file){
        try {
        	File exePath = new File(DOC2DOCX_PATH);
        	System.out.println(exePath.getAbsolutePath());
        	final String cmdStr = exePath.getAbsolutePath() + " " + file.getAbsolutePath();
        	System.out.println(cmdStr);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						process = Runtime.getRuntime().exec(cmdStr);
						process.waitFor();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			t.start();
			t.join(10000);
			File newfile = new File(file.getAbsolutePath() + "x");
			if(newfile.exists()){
				System.out.println("ת���ɹ�: " + newfile.getAbsolutePath());
				extractFromDocx(newfile);
			}else{
				System.out.println("ת��ʧ��: " + file.getAbsolutePath());
				if(process != null){
					process.destroy();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void extractFromDocx(File file){
		FileInputStream fis = null;
		XWPFDocument doc = null;
		try {
			System.out.println(file.getAbsolutePath());
			writeToExcel(0, "" + rowIndex);
			if(file.getName().indexOf("ļ��")>0){
				writeToExcel(1, file.getName().substring(0, file.getName().indexOf("ļ��")));
			}else{
				writeToExcel(1, file.getName());
			}
			
			fis = new FileInputStream(file);
			doc = new XWPFDocument(fis);
			Iterator<IBodyElement> iterator = doc.getBodyElementsIterator();
			IBodyElement element;
			XWPFParagraph paragraph;
			String unit = "";
			XWPFTable table;
			while(iterator.hasNext()){
				element = iterator.next();
				if(BodyElementType.PARAGRAPH.equals(element.getElementType())){
					paragraph = (XWPFParagraph) element;
					if(isTargetTable(paragraph.getText())){//�ҵ����ϵı�����
						//System.out.println(paragraph.getText());
						while(iterator.hasNext()){
							element = iterator.next();
							if(BodyElementType.TABLE.equals(element.getElementType())){//�ҵ������������ı��
								table = (XWPFTable) element;
								boolean isTableHead = true;
								Map<String,Integer> yearMap = new HashMap<String,Integer>();
								for(XWPFTableRow row:table.getRows()){
									if(isTableHead){//��ȡ��ݶ�Ӧ���к�
										int index = 0;
										for(XWPFTableCell cell : row.getTableCells()){
											String year = isTargetColumn(cell.getText());
											if(year != null){
												yearMap.put(year, index);
											}
											index++;
										}
										isTableHead = false;
										/*for(String year:Parameter.YEARS){
											System.out.print("\t" + year);
										}*/
										//System.out.println();
									}else{//������Ҫ�����Ի�ȡ����
										String subject = isTargetSubject(row.getCell(0).getText());
										if(subject != null){
											//System.out.print(row.getCell(0).getText() + "\t");
											for(String year : Parameter.YEARS){
												if(yearMap.containsKey(year)){
													writeToExcel(2 + getIndexFromArray(year, Parameter.YEARS) * (Parameter.SUBJECTS.length + 1), unit);
													writeToExcel(3 + getIndexFromArray(year, Parameter.YEARS) * (Parameter.SUBJECTS.length + 1) + 
															getIndexFromArray(subject, Parameter.SUBJECTS), 
															row.getCell(yearMap.get(year)).getText().replace("%", ""));
													//System.out.print(row.getCell(yearMap.get(year)).getText() + "\t");
												}
											}
											//System.out.println();
										}
									}
									
								}
								break;
							}else if(BodyElementType.PARAGRAPH.equals(element.getElementType()) && "".equals(unit)){//��ȡ��λ
								unit = ((XWPFParagraph)element).getText().trim().replace("��", "");
								if(!unit.contains("��λ")){
									unit = "";
								}
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(doc != null)doc.close();
				if(fis != null)fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static int getIndexFromArray(String element, String[] array){
		if(element == null || array == null){
			return -1;
		}
		int i = 0;
		for(String tmp : array){
			if(tmp.equals(element)){
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public static boolean isTargetTable(String name){
		for(String table:Parameter.TABLES){
			if(name.contains(table)){
				return true;
			}
		}
		return false;
	}
	
	public static String isTargetSubject(String name){
		name = name.replaceAll("\\s*", "");
		name = name.replaceAll("��", "");
		for(String subject:Parameter.SUBJECTS){
			if(name.trim().startsWith(subject)){
				return subject;
			}
		}
		return null;
	}
	
	public static String isTargetColumn(String text){
		for(String year:Parameter.YEARS){
			if(text.contains(year)){
				return year;
			}
		}
		return null;
	}
	
	public static void initExcelWriter(){
		book = new HSSFWorkbook();
		sheet = book.createSheet("ExtractorData");
		rowIndex = 0;
		row = sheet.createRow(rowIndex);
		row.createCell(0).setCellValue("���");
		row.createCell(1).setCellValue("��Ʊ����");
		for(int i = 0; i < Parameter.YEARS.length; i++){
			row.createCell(2 + i * (Parameter.SUBJECTS.length + 1)).setCellValue(Parameter.YEARS[i]);
			for(int j = 0; j < Parameter.SUBJECTS.length; j++){
				row.createCell(3 + i * (Parameter.SUBJECTS.length + 1) + j).setCellValue(Parameter.SUBJECTS[j]);
			}
		}
		rowIndex++;
	}
	
	public static void writeToExcel(int col, String content){
		row.createCell(col).setCellValue(content);
	}
	
	public static void exportExcel(){
		File file = new File(ROOT_DIR_PATH + "/" + "Extractor.xls");
		System.out.println("Export Excel: " + file.getAbsolutePath());
		try {
			if(!file.exists()){
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			book.write(fos);
			book.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
