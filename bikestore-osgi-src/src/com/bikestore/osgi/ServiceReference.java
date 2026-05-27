package com.bikestore.osgi;
import java.util.Properties;
public class ServiceReference<T> {
    private final Class<T> serviceClass;
    private final Properties props;
    public ServiceReference(Class<T> sc, Properties p) { this.serviceClass=sc; this.props=p; }
    public Class<T> getServiceClass() { return serviceClass; }
    public Object getProperty(String key) { return props!=null ? props.get(key) : null; }
}
