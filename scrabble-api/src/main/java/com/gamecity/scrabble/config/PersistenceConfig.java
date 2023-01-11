package com.gamecity.scrabble.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring configuration for Hibernate including transaction management, entity management, jpa
 * properties, datasource properties and data/schema populator
 * 
 * @author ekarakus
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:database.properties")
@PropertySource("classpath:hibernate.properties")
public class PersistenceConfig {

    private static final String CREATE_DROP = "create-drop";

    @Autowired
    private Environment env;

    @Bean
    PlatformTransactionManager transactionManager() {
        final EntityManagerFactory factory = entityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource());
        factory.setJpaVendorAdapter(vendorAdapter());
        factory.setPackagesToScan("com.gamecity.scrabble.entity");
        factory.setJpaProperties(jpaProperties());
        factory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
        return factory;
    }

    private JpaVendorAdapter vendorAdapter() {
        final HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);
        return jpaVendorAdapter;
    }

    private Properties jpaProperties() {
        final Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        jpaProperties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        jpaProperties.put("hibernate.format_sql", env.getProperty("hibernate.format_sql"));
        jpaProperties.put("hibernate.enable_lazy_load_no_trans",
                env.getProperty("hibernate.enable_lazy_load_no_trans"));
        return jpaProperties;
    }

    /**
     * Exception translator for hibernate exception
     * 
     * @return the exception translator
     */
    @Bean
    HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    /**
     * Gets the {@link DataSource} used for jdbc connection
     * 
     * @return the data source
     */
    @Bean
    DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.username"));
        dataSource.setPassword(env.getProperty("jdbc.password"));
        return dataSource;
    }

    /**
     * Gets the {@link DataSourceInitializer} for hibernate configuration
     * 
     * @param dataSource dataSource used for initialization
     * @return the data source initializer
     */
    @Bean
    DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        final DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(databasePopulator());

        final boolean createSchema = env.getProperty("hibernate.create_schema", Boolean.class);
        final String hbm2ddlAuto = env.getProperty("hibernate.hbm2ddl.auto", String.class);
        dataSourceInitializer.setEnabled(createSchema || CREATE_DROP.equals(hbm2ddlAuto));
        return dataSourceInitializer;
    }

    // insert sample data and create database on application start
    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        boolean createSchema = env.getProperty("hibernate.create_schema", Boolean.class);
        if (createSchema) {
            databasePopulator.addScript(new ClassPathResource("database.sql"));
        }
        databasePopulator.addScript(new ClassPathResource("gamedata.sql"));
        return databasePopulator;
    }

}
