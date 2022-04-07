package com.hao.postgres;

import com.hao.postgres.util.HibernateFilterHelper;
import com.hao.postgres.util.MyJpaRepositoryImpl;
import com.hao.postgres.util.SecurityContextUtils;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EntityScan("com.hao.postgres.jpa.entity")
@EnableJpaRepositories(repositoryBaseClass = MyJpaRepositoryImpl.class, basePackages = "com.hao.postgres", enableDefaultTransactions = true)
@EnableTransactionManagement
// @EnableJpaAuditing
@Slf4j
public class PostgresApplication {

	@Bean
	@ConditionalOnMissingBean
	public PlatformTransactionManager transactionManager(ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
		log.info("Creating transaction manager ...");
		JpaTransactionManager transactionManager = new MyJpaTransactionManager();
		transactionManagerCustomizers.ifAvailable(customizers -> customizers.customize(transactionManager));

		return transactionManager;
	}

	static void enableFilter(final EntityManager entityManager) {
		if (HibernateFilterHelper.shouldEnableTenantFilter()) {
			Session session = entityManager.unwrap(Session.class);
			HibernateFilterHelper.enableTenantFilter(session);
			log.info("enabled tenant filter on entity manager created for transaction [tenantId: {}]",
					SecurityContextUtils.getTenantId());
		}
	}

	private static class MyJpaTransactionManager extends JpaTransactionManager {
		private static final long serialVersionUID = 7335436515694602183L;

		@Override
		protected EntityManager createEntityManagerForTransaction() {
			log.info("Creating Entity Manager for TX...");
			final EntityManager entityManager = super.createEntityManagerForTransaction();
			log.info("Entity Manager [{}], created", entityManager);
			enableFilter(entityManager);
			return entityManager;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(PostgresApplication.class, args);
	}

}
