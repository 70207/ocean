import com.tt.ocean.config.Config;

import com.tt.ocean.config.route.ConfigRoute;
import com.tt.ocean.net.Server;
import com.tt.ocean.route.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;

public class ConfigApp {
    private static final Logger log = LogManager.getLogger(ConfigApp.class.getName());

    private static  int __config_server_port = 1213;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        if (bundle == null) {
            throw new IllegalArgumentException(
                    "[cache.properties] is not found!");
        }
        int port = Integer.valueOf(bundle.getString("config.server.port"));

        log.info("server port:" + port);
        if(port > 0){
            __config_server_port = port;
        }

    }



    public static void main(String[] args){

        Route route = new ConfigRoute(new Config());
        Server server = new Server();

        try {
            server.start( route,__config_server_port);
        }
        catch(Exception ex){
            log.error("server run exception, ex:"+ex.getMessage());
        }
    }
}
