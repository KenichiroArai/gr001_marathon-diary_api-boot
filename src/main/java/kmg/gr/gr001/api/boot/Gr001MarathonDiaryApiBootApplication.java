package kmg.gr.gr001.api.boot;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * マラソン日記 REST API の起動クラス<br>
 * <p>
 * api（Controller）と domain をスキャンします。DB 実装（db-xxx）は自動設定で登録されるため、ここでは指定しません。
 * </p>
 *
 * @author KenichiroArai
 *
 * @since 0.1.0
 *
 * @version 0.1.0
 */
@SpringBootApplication(scanBasePackages = {
    "kmg.gr.gr001.api", "kmg.gr.gr001.domain",
})
public class Gr001MarathonDiaryApiBootApplication {

    /**
     * エントリポイント
     *
     * @since 0.1.0
     *
     * @param args
     *             引数
     */
    @SuppressWarnings({
        "resource",
    })
    public static void main(final String[] args) {

        // SpringApplicationの設定
        final SpringApplication application = new SpringApplication(Gr001MarathonDiaryApiBootApplication.class);
        final Properties        properties  = new Properties();
        application.setDefaultProperties(properties);

        application.run(args);

    }

    /**
     * コンストラクタ
     *
     * @since 0.1.0
     */
    public Gr001MarathonDiaryApiBootApplication() {

        // 処理なし
    }

}
