package ca.bc.gov.ols.router.admin;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.stream.JsonWriter;

import ca.bc.gov.ols.config.ConfigurationComparison;
import ca.bc.gov.ols.config.ConfigurationParameter;
import ca.bc.gov.ols.config.ConfigurationStore;
import ca.bc.gov.ols.config.FileExportConfigurationStore;

@RestController
public class AdminController {
	static final Logger logger = LoggerFactory.getLogger(
			AdminController.class.getCanonicalName());
	
	@Autowired
	private AdminApplication adminApp;
	
	@GetMapping(value = "/export", produces = "application/json")
	public void doExport(HttpServletResponse response) throws IOException {
		ConfigurationStore configStore = adminApp.getConfigStore();
		
		// export all of the data in the database
		response.addHeader("Content-Type", "application/json");
		response.addHeader("Content-Disposition", "attachment; filename=ols_admin_config_export.json");
		response.setCharacterEncoding("UTF-8");
		JsonWriter jw = new JsonWriter(response.getWriter());
		jw.setIndent("  ");
		jw.beginObject();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		jw.name("exportDate").value(dateFormat.format(new Date()));
		int rowCount;
		
		// BGEO_CONFIGURATION_PARAMETERS
		jw.name("BGEO_CONFIGURATION_PARAMETERS");
		jw.beginObject();
		Stream<ConfigurationParameter> configParams = configStore.getConfigParams();
		jw.name("rows");
		jw.beginArray();
		rowCount = 0;
		for (Iterator<ConfigurationParameter> it = configParams.iterator(); it.hasNext();) {
			rowCount++;
			ConfigurationParameter param = it.next();
			jw.beginObject();
			jw.name("app_id").value(param.getAppId());
			jw.name("config_param_name").value(param.getConfigParamName());
			jw.name("config_param_value").value(param.getConfigParamValue());
			jw.endObject();
		}
		jw.endArray();
		jw.name("rowCount").value(rowCount);
		jw.endObject();		

		jw.endObject();
		jw.flush();
		jw.close();
	}
	
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public ModelAndView doValidate(@RequestParam("file") MultipartFile file) {
		ConfigurationStore exportConfig = new FileExportConfigurationStore(file);
		ConfigurationStore localConfig = adminApp.getConfigStore();
		ConfigurationComparison comparison = new ConfigurationComparison(localConfig, exportConfig);
		ModelAndView modelAndView = new ModelAndView("view/validate", "exportConfig", exportConfig);
		modelAndView.addObject("comparison", comparison);
		return modelAndView;
	}

	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public ModelAndView doImport(@RequestParam("file") MultipartFile file) {
		FileExportConfigurationStore exportConfig = new FileExportConfigurationStore(file);
		if(exportConfig.getErrors().isEmpty()) {
			adminApp.getConfigStore().replaceWith(exportConfig);
			return new ModelAndView("view/import", "errors", exportConfig.getErrors());
		}
		return new ModelAndView("view/import", "messages", exportConfig.getMessages());
	}
	
}
