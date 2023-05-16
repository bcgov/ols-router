package ca.bc.gov.ols.router.rdm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class RdmFetcher {

	public static void main(String[] args) {
		RdmFetcher fetcher = new RdmFetcher();
		fetcher.convert();
	}
	
	void convert() {
		
	}
	
	void fetch() throws IOException {
		String urlString = "https://dev-rdm.th.gov.bc.ca/api/view/restrictions_active";
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("accept", "application/json");
		conn.connect();
		
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();
	}

}
