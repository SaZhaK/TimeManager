package Application.Controllers;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class HWController {

    @Hidden
    @PostMapping("/hw")
    public String helloWorld(HttpServletRequest request) {
        JSONObject responseJson = new JSONObject();
        try {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                data.append(line);
            }

            JSONObject receivedDataJson = new JSONObject(data.toString());

            int day = receivedDataJson.getInt("day");
            JSONObject timeManager = receivedDataJson.getJSONObject("timeManager");
            String text = timeManager.getString("text");

            log.info("Day " + day);
            log.info("Time manager " + text);

            responseJson.put("status", "success");
        } catch (Exception e) {
            log.error("unknown error: " + e.getMessage());
            responseJson.put("status", "unknown error");
        }

        return responseJson.toString();
    }
}
