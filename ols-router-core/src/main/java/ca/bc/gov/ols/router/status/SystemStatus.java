package ca.bc.gov.ols.router.status;

import java.time.ZonedDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import ca.bc.gov.ols.router.config.RouterConfig;
import ca.bc.gov.ols.rowreader.DateType;

@Schema(description = "The current status of the routing service, including its version and dataset timestamps.")
public class SystemStatus {
	
	public final String version = RouterConfig.VERSION;
	public final String gitCommitId = RouterConfig.GIT_COMMIT_ID;
	public String dataProcessingTimestamp;
	public String roadNetworkTimestamp;

	public String startTimestamp = "";
	
	public String rdmLastSuccessfulUpdate = "";
	public String rdmLastFailedUpdate = "";
	public int rdmSuccessfulUpdateCount = 0;
	public int rdmFailedUpdateCount = 0;
	public int rdmLastRecordCount = 0;
	
	public void setDates(Map<DateType, ZonedDateTime> dates) {
		if(dates != null) {
			dataProcessingTimestamp = String.valueOf(dates.get(DateType.PROCESSING_DATE));
			roadNetworkTimestamp = String.valueOf(dates.get(DateType.ITN_VINTAGE_DATE));
		}
	}
	
}
