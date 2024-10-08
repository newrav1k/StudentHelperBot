package com.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;

@Slf4j
public class HibernateUtil {

    private HibernateUtil() {
    }

    ///org.hibernate.service.spi.ServiceException: Unable to create requested service [org.hibernate.engine.jdbc.env.spi.JdbcEnvironment] due to: Error calling Driver.connect() [n/a]
    /// 	at org.hibernate.service.internal.AbstractServiceRegistryImpl.createService(AbstractServiceRegistryImpl.java:276) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.service.internal.AbstractServiceRegistryImpl.initializeService(AbstractServiceRegistryImpl.java:238) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.service.internal.AbstractServiceRegistryImpl.getService(AbstractServiceRegistryImpl.java:215) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.boot.model.relational.Database.<init>(Database.java:45) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.boot.internal.InFlightMetadataCollectorImpl.getDatabase(InFlightMetadataCollectorImpl.java:226) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.boot.internal.InFlightMetadataCollectorImpl.<init>(InFlightMetadataCollectorImpl.java:194) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.boot.model.process.spi.MetadataBuildingProcess.complete(MetadataBuildingProcess.java:171) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.boot.model.process.spi.MetadataBuildingProcess.build(MetadataBuildingProcess.java:129) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.boot.internal.MetadataBuilderImpl.build(MetadataBuilderImpl.java:449) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.boot.internal.MetadataBuilderImpl.build(MetadataBuilderImpl.java:101) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.cfg.Configuration.buildSessionFactory(Configuration.java:949) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at org.hibernate.cfg.Configuration.buildSessionFactory(Configuration.java:999) ~[hibernate-core-6.6.1.Final.jar:6.6.1.Final]
    /// 	at com.example.utils.HibernateUtil.getSessionFactory(HibernateUtil.java:17) ~[classes/:na]
    public static SessionFactory getSessionFactory() {
        Configuration configuration = new Configuration().configure();
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());
        return configuration.buildSessionFactory();
    }
}