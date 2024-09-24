package ru.tehnobear.essence.dao;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.postgresql.PGProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static net.ttddyy.dsproxy.listener.logging.Log4jLogLevel.*;

@Configuration
@EnableConfigurationProperties(DBProperties.class)
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.tehnobear.essence.dao", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
public class DBConfiguration {

    private final DBProperties dbProperties;
    private static final String SQL_LOGGER = "ru.tehnobear.essence.dao.ProxyDataSource";

    @Bean
    @Primary
    public DataSource dataSource() {
        this.dbProperties.setRegisterMbeans(true);
        this.dbProperties.setAutoCommit(false);
        if (this.dbProperties.getDriverClassName().equalsIgnoreCase("org.postgresql.Driver")) {
            this.dbProperties.addDataSourceProperty(PGProperty.REWRITE_BATCHED_INSERTS.getName(), true);
        }
        HikariDataSource ds = new HikariDataSource(this.dbProperties);

        return ProxyDataSourceBuilder.create(ds)
                .countQuery()
                .logQueryByLog4j(TRACE, SQL_LOGGER)
                .logSlowQueryByLog4j(600, TimeUnit.SECONDS, WARN, SQL_LOGGER)
                .logSlowQueryByLog4j(1200, TimeUnit.SECONDS, ERROR, SQL_LOGGER)
                .multiline()
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(getClass().getPackageName());

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        Properties jpa = this.dbProperties.getJpa() == null ? new Properties() : this.dbProperties.getJpa();
        jpa.put("hibernate.default_schema", this.dbProperties.getSchema());
        em.setJpaProperties(jpa);
        em.setEntityManagerFactoryInterface(EntityManagerFactory.class);
        return em;
    }

    @Bean
    @Primary
    public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return txManager;
    }

    @Bean
    public JPAQueryFactory queryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public TransactionTemplate transactionTemplate(JpaTransactionManager transactionManage) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManage);
        transactionTemplate.setTimeout((int) this.dbProperties.getDefaultTxTimeout().getSeconds());
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }
}
