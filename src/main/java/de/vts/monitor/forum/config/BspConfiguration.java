package de.vts.monitor.forum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "bsp")
public class BspConfiguration {

    private String zipCheckupCron;

    private Dih dih = new Dih();
    private Zam zam = new Zam();
    private String fusionList;
    private ExternalService externalService = new ExternalService();

    // === Nested Configuration Classes ===

    public static class Dih {
        private S3 s3 = new S3();
        private Login login = new Login();

        public S3 getS3() { return s3; }

        public void setS3(S3 s3) { this.s3 = s3; }

        public Login getLogin() { return login; }

        public void setLogin(Login login) { this.login = login; }
    }

    public static class Zam {
        private List<String> mandantList;
        private List<String> instanceList;

        public List<String> getMandantList() { return mandantList; }

        public void setMandantList(List<String> mandantList) { this.mandantList = mandantList; }

        public List<String> getInstanceList() { return instanceList; }

        public void setInstanceList(List<String> instanceList) { this.instanceList = instanceList; }
    }

    public static class S3 {
        private String endpoint;
        private URL loginTokenUrl;
        private String bucket;
        private String prefix;

        public String getEndpoint() { return endpoint; }

        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

        public URL getLoginTokenUrl() {
            return loginTokenUrl;
        }

        public void setLoginTokenUrl(URL loginTokenUrl) { this.loginTokenUrl = loginTokenUrl; }

        public String getBucket() { return bucket; }

        public void setBucket(String bucket) { this.bucket = bucket; }

        public String getPrefix() { return prefix; }

        public void setPrefix(String prefix) { this.prefix = prefix; }
    }

    public static class Login {
        private String user;
        private String passwd;

        public String getUser() { return user; }

        public void setUser(String user) { this.user = user; }

        public String getPasswd() { return passwd; }

        public void setPasswd(String passwd) { this.passwd = passwd; }
    }

    public String getZipCheckupCron() { return zipCheckupCron; }

    public void setZipCheckupCron(String zipCheckupCron) { this.zipCheckupCron = zipCheckupCron; }

    public Dih getDih() { return dih; }

    public void setDih(Dih dih) { this.dih = dih; }

    public Zam getZam() { return zam; }

    public void setZam(Zam zam) { this.zam = zam; }

    public String getFusionList() { return fusionList; }

    public void setFusionList(String fusionList) { this.fusionList = fusionList; }

    public ExternalService getExternalService() { return externalService; }

    public void setExternalService(ExternalService externalService) { this.externalService = externalService; }

    public static class ExternalService {
        private String url = "";
        private String apiKey = "";

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }

        public String getApiKey() { return apiKey; }

        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }
}
