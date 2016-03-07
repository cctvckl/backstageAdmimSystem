package backstage_op_web;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.kankan.op.cache.RuleMgtDicCache;

public class TestJedis {
	@Test
	public void testJedis(){
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("mvc-dispatcher-servlet.xml");
		RuleMgtDicCache activityIdManagementDao = (RuleMgtDicCache) applicationContext
				.getBean("ruleMgtDicCache");
		activityIdManagementDao.jedisTemplateOPRead.set("ckl", "222");
		String result = activityIdManagementDao.jedisTemplateOPRead.get("ckl");
		System.out.println(result);
	}
}
