package org.exobel.routerkeygen.thomsonGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;



public class Stage3 {

	public static void main(String[] args) {
		FileInputStream fis;
		FileOutputManager files = new FileOutputManager();
		EntryTable entry = new EntryTable(3);
    	System.out.println("Stage3");
		System.out.println("Creating secondary tables.");
		long begin = System.currentTimeMillis();
		String fileName = "56.dat";
		int progress = 0;
		int c = 0;
		byte [] fileData = new  byte [300000];
		byte [] outputData = new  byte [300000];
		byte [] webDicTable = new byte[768]; 
		RandomAccessFile webDicIndex = null;
		try {
			File webDic = new File("webdic.dic");
			webDic.delete();
			webDic.createNewFile();
			webDicIndex = new RandomAccessFile(webDic, "rw");
			webDicIndex.setLength(1024 + 256 * 768);
			webDicIndex.seek(1024);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int a = 0; a < AlphabetCodes.charect.length; a++)
        {
            for(int b = 0; b < AlphabetCodes.charect.length; b++ , c++)
            {
           	fileName = AlphabetCodes.charect[a] + AlphabetCodes.charect[b] + ".dat";
				try {
					fis = new FileInputStream(fileName);
				} catch (FileNotFoundException e) {
		            System.out.println("Error!" + e);
					return;
				}
				int count = 0;
				
				try {
					count = fis.read(fileData);
					fis.close();
					files.addFile(fileName);
				} catch (IOException e) {
					System.out.println("Error!" + e);
					return;
				}
				byte currentEntry;
				byte tmp;
				int offset= 0;
				int address = 1024;//size of the table
				//1024 = 256 * ( 1 + 3)
				// 1byte for header and 3 for address
				currentEntry = fileData[offset + 0];
				entry.addEntry((short) (0xFF & currentEntry), address );
				outputData[address + 0] = fileData[offset + 2];
				outputData[address + 1] = fileData[offset + 3];
				address += 2;
				offset += 4;
				while (offset < count ){
					tmp = fileData[offset + 0];
					if ( tmp != currentEntry )
					{ 
						currentEntry = tmp;
						entry.addEntry((short) (0xFF & currentEntry), address );
					}
					outputData[address + 0] = fileData[offset + 2];
					outputData[address + 1] = fileData[offset + 3];
					offset += 4;
					address += 2;
				}
				entry.toFile(outputData);
				entry.toWebDic(webDicTable);
				files.sendFile(fileName, outputData , address);
				try {
					webDicIndex.write(webDicTable);
				} catch (IOException e) {
					e.printStackTrace();
				}
				progress = (c *100)>>8;
				System.out.println("File " + fileName + " processed " +
				           "  Total done: " + progress + "% " );
			}
		}
        files.close();
		long time = System.currentTimeMillis() - begin;
        System.out.println("Done .. 100%! It took " + time + " miliseconds.");
	}		
}
