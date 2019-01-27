import cn.hutool.core.util.RandomUtil;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import quick.pager.common.constants.Constants;
import quick.pager.common.dto.SmsDTO;
import quick.pager.common.service.RedisService;
import quick.pager.shop.model.common.SmsTemplate;
import quick.pager.shop.model.user.User;
import quick.pager.shop.user.UserApplication;
import quick.pager.shop.user.mapper.SmsTemplateMapper;
import quick.pager.shop.user.mq.MqService;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = UserApplication.class)
public class UserApplicationTests {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MqService mqService;
    @Autowired
    private SmsTemplateMapper smsTemplateMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testPub() {
        stringRedisTemplate.convertAndSend("hello", "I am come from redis message!");
    }

    @Test
    public void testRedisHash() {
        User user = new User();
        user.setPassword("33333");
        user.setPhone("323234999");
        user.setId(444L);
        String token = RandomUtil.randomUUID().replace("-", "");
        System.out.println(token);
        redisService.setValueOps(String.valueOf(user.getId()), token, 10 * 24 * 60 * 60);

        System.out.println(redisService.getValueOps(String.valueOf(user.getId())));
    }

    @Test
    public void testMq() throws IOException {

        List<SmsTemplate> smsTemplates = smsTemplateMapper.selectByModule("user", Constants.SMS.INITIAL_CIPHER_SMS);
        SmsTemplate smsTemplate = smsTemplates.get(0);
        String content = MessageFormat.format(smsTemplate.getSmsTemplateContent(), "13818471341", "22343");
        SmsDTO smsdto = new SmsDTO();
        smsdto.setPhone("13818471341");
        smsdto.setContent(content);
        mqService.sender(Constants.RabbitQueue.SEND_SMS, smsdto);
        System.in.read();
    }


    private Properties pro;

    @Before
    public void before() throws IOException {
        InputStream resource = this.getClass().getResourceAsStream("test.properties");
        pro = new Properties();
        pro.load(resource);
    }
    @Test
    public void testProperties() {
        test("shop-zuul");
//        test("shop-activity");
//        test("shop-goods");
//        test("shop-manage");
//        test("shop-order");
//        test("shop-settlement");
    }


    private void test(String serviceId) {
        for (Map.Entry<Object, Object> me: pro.entrySet()) {
            StringBuilder builder = new StringBuilder("insert into `pager_config`.`t_config` (`label`, `profile`, `service_id`, `app_key`, `app_value`) values ('master', 'dev',");
            builder.append("'");
            builder.append(serviceId);
            builder.append("'").append(",");
            builder.append("'");
            builder.append(me.getKey());
            builder.append("'");
            builder.append(",");
            builder.append("'");
            builder.append(me.getValue());
            builder.append("');");
            System.out.println(builder.toString());
        }
    }

}