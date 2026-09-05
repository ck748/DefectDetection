package com.ggbond.defectdetection;

import com.ggbond.defectdetection.common.SpringContextUtil;
import com.ggbond.defectdetection.dto.DetectResDto;
import com.ggbond.defectdetection.pojo.Check;
import com.ggbond.defectdetection.pojo.Defection;
import com.ggbond.defectdetection.software.face.GUIAttributes;
import com.ggbond.defectdetection.software.face.MainInterface;
import com.ggbond.defectdetection.util.ImgUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//@RunWith(SpringRunner.class)
//@SpringBootTest()
class DefectDetectionApplicationTests {

    @Test
    void contextLoads() {
    }


    @Autowired
    MainInterface mainInterface;

    @Test
    void maininterfaceTest(){

    }

    @Autowired
    GUIAttributes atrributes;

    @Test
    void testToString() {
        System.out.println(atrributes);
    }

    @Test
    void datasourceTest() {
        try {
            // 手动创建数据源（需要根据实际配置调整参数）
            com.zaxxer.hikari.HikariDataSource dataSource = new com.zaxxer.hikari.HikariDataSource();
            dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/defect_detection?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC");
            dataSource.setUsername("root");
            dataSource.setPassword("000000");
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

            java.sql.Connection connection = dataSource.getConnection();
            boolean isValid = connection.isValid(5);

            if (isValid) {
                System.out.println("数据库连接测试成功！");
            } else {
                System.err.println("数据库连接无效！");
            }

            connection.close();
            dataSource.close();

        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }




    @Test
    void convertTOCheckTest(){

        DetectResDto res=new DetectResDto();
        String imgBase64="";

        File folder=new File("src/main/resources/testImg/JPEGImages");
        File[] files=folder.listFiles();
        Random random = new Random();
        assert files != null;
        int randomIndex = random.nextInt(files.length);
        File img=files[randomIndex];
        try {
            imgBase64= ImgUtil.imageToBase64ByPath(img.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        res.setImgBase64(imgBase64);

        List<Defection> defectionList=new ArrayList<>();

        String[] category={"划痕","凹陷","突出","抛光"};

        if(random.nextBoolean()) {

            int defectiveSum = random.nextInt(5);
            for (int i = 0; i < defectiveSum; i++) {
                Defection defection = new Defection(null,
                        random.nextDouble(1.),
                        random.nextDouble(1),
                        random.nextDouble(1),
                        random.nextDouble(1),
                        random.nextDouble(1),
                        null,
                        category[random.nextInt(4)],
                        null,
                        null,
                        null
                );
                defectionList.add(defection);
            }
        }
        res.setDefections(defectionList);
        res.setDefectionsSum(res.getDefections().size());

        Check.convertTOCheck(res);

    }


}
