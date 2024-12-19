package it.polimi.tiw.albums.utils;

import jakarta.servlet.http.Part;

public final class VirusScanner{
	
	private VirusScanner() {
		
	}
	
	public static boolean containsVirus(Part filePart) {
		//TODO Implement anti virus solution in docker 
		
		/*System.out.println("Initializing ClamavClient");
		ClamavClient client = new ClamavClient("localhost");
		System.out.println("ClamavClient initialized");
		ScanResult result = null;
		
		try(InputStream iStream = filePart.getInputStream()){
			System.out.println("Scanning file...");
			result = client.scan(iStream, 1024 * 1024 * 10);
			System.out.println("File scanned.");
		} catch (IOException e) {
			System.out.println("IOException caught in virus scanner");
			e.printStackTrace();
		}
		
		System.out.println("Evaluating results...");
		if (result instanceof ScanResult.OK) {
			System.out.println("File does not contain viruses");
		    return false;
		}
		else {
			System.out.println("File contains viruses");
		}*/
		
		return false;
	}
}