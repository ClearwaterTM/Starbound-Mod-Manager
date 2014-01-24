package main.java.net.krazyweb.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import main.java.net.krazyweb.helpers.FileCopier.TreeCopier;

import org.apache.log4j.Logger;

public class FileHelper {
	
	private static final Logger log = Logger.getLogger(FileHelper.class);
	
	/*
	 * File signatures found at: http://www.garykessler.net/library/file_sigs.html
	 */
	private static final char[] SIG_SEVENZIP = new char[] { 0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C };
	private static final char[] SIG_RAR = new char[] { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00 };
	private static final char[] SIG_ZIP = new char[] { 0x50, 0x4B, 0x03, 0x04 };

	public static boolean copyFile(Path src, Path dest) {
		
		dest = Files.isDirectory(src) ? dest.resolve(src) : dest;
		
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        TreeCopier tc = new TreeCopier(src, dest, false, false);
        
        try {
			Files.walkFileTree(src, opts, Integer.MAX_VALUE, tc);
			return true;
		} catch (IOException e) {
			log.error("Copying file/folder: " + src + " to " + dest, e);
		}
        
        return false;
		
	}
	
	public static final boolean verify(final Path path) {
		
		if (path == null) {
			return false;
		}
		
		String fileName = path.getFileName().toString();
		String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
		
		int byteOffset = 0;
		char[] signatureBytes;
		
		switch (extension) {
			case ".7z":
				signatureBytes = SIG_SEVENZIP;
				break;
			case ".rar":
				signatureBytes = SIG_RAR;
				break;
			case ".zip":
				signatureBytes = SIG_ZIP;
				break;
			default:
				return false;
		}
		
		byte[] fileBytes = null;
		
		try {
			fileBytes = Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("Reading all bytes from a file to get the signature.", e);
			return false;
		}
		
		if (fileBytes == null) {
			return false;
		}
		
		for (int i = 0; i < signatureBytes.length; i++) {
			if (fileBytes[i + byteOffset] != (char) signatureBytes[i]) {
				return false;
			}
		}
		
		log.info("File '" + path + "' verified.");
		
		return true;
		
	}
	
	public static long getChecksum(final Path path) throws IOException {
		
		FileInputStream inputFile = new FileInputStream(path.toFile());
		CheckedInputStream checkedStream = new CheckedInputStream(inputFile, new Adler32());
		BufferedInputStream input = new BufferedInputStream(checkedStream);
		
		while (input.read() != -1) {
			//Do nothing; simply reading file contents.
		}
		
		input.close();
		checkedStream.close();
		inputFile.close();
		
		long checksum = checkedStream.getChecksum().getValue();
		
		log.info("Checksum (" + checksum + ") created for file: " + path);
		
		return checksum;
		
	}
	
	public static Set<Path> listFiles(final String directory, final Set<Path> paths) {
		return listFiles(Paths.get(directory), paths);
	}
	
	public static Set<Path> listFiles(final Path directory, final Set<Path> paths) {
		
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory.toUri()))) {
            for (Path path : directoryStream) {
            	paths.add(path);
            }
        } catch (IOException e) {
        	log.error("Listing files in directory:" + directory, e);
        }
		
        return paths;
        
	}
	
	public static void deleteFile(final Path path) throws IOException {
		
		//TODO Change to fully use nio
		
		if (Files.isDirectory(path)) {
			
			File[] children = path.toFile().listFiles();
			
			if (children != null) {
				for (File child : path.toFile().listFiles()) {
					deleteFile(child.toPath());
				}
			}
			
		}
		
		Files.deleteIfExists(path);
		
	}
	
	public static boolean isJSON(String filename) {
		
		if (filename.endsWith(".png") || filename.endsWith(".wav") || filename.endsWith(".ogg") || filename.endsWith(".txt") || filename.endsWith(".lua") || filename.endsWith(".ttf")) {
			return false;
		}
		
		return true;
		
	}
	
	public static String fileToString(File file) {
		
		// TODO update to use nio
	
		String output = "";
		
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {

			String line;

			while ((line = in.readLine()) != null) {
				output += line + "\r\n";
			}
			
		} catch (IOException e) {
			log.error("Reading a file to a string.", e);
			return null;
		}

		return output;
	
	}

}