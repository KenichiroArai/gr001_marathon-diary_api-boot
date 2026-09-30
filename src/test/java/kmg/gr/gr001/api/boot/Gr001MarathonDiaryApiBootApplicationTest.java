package kmg.gr.gr001.api.boot;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import kmg.gr.gr001.domain.sample.repository.SampleGreetingRepository;

/**
 * Gr001MarathonDiaryApiBootApplication のテスト<br>
 * <p>
 * DB への接続は発生しない（接続はリクエスト時に行われる）ため、DB を起動せずに実行できます。
 * </p>
 *
 * @author KenichiroArai
 *
 * @since 0.1.0
 *
 * @version 0.1.0
 */
@SuppressWarnings({
    "nls", "static-method",
})
@SpringBootTest
public class Gr001MarathonDiaryApiBootApplicationTest {

    /**
     * アプリケーションコンテキスト
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * デフォルトコンストラクタ
     *
     * @since 0.1.0
     */
    public Gr001MarathonDiaryApiBootApplicationTest() {

        // 処理なし
    }

    /**
     * コンストラクタのテスト - 正常系:インスタンスを生成できる場合
     *
     * @since 0.1.0
     */
    @Test
    public void testConstructor_normal() {

        /* 期待値の定義 */

        /* 準備 */

        /* テスト対象の実行 */
        final Gr001MarathonDiaryApiBootApplication testTarget = new Gr001MarathonDiaryApiBootApplication();

        /* 検証の準備 */

        /* 検証の実施 */
        Assertions.assertNotNull(testTarget, "インスタンスが生成されていません");

    }

    /**
     * コンテキスト起動のテスト - 正常系:DB 実装のリポジトリが自動設定で登録される場合
     *
     * @since 0.1.0
     */
    @Test
    public void testContextLoads_normalRepositoryRegistered() {

        /* 期待値の定義 */
        final int expectedCount = 1;

        /* 準備 */

        /* テスト対象の実行 */

        /* 検証の準備 */
        final int actualCount = this.applicationContext.getBeanNamesForType(SampleGreetingRepository.class).length;

        /* 検証の実施 */
        Assertions.assertEquals(expectedCount, actualCount, "リポジトリ実装が登録されていません");

    }

    /**
     * main メソッドのテスト - 正常系:SpringApplication が起動される場合
     *
     * @since 0.1.0
     */
    @SuppressWarnings({
        "resource", "unused"
    })
    @Test
    public void testMain_normalRun() {

        /* 期待値の定義 */
        final int expectedConstructionCount = 1;

        /* 準備 */
        final String[] testArgs = {};

        /* テスト対象の実行 */
        try (MockedConstruction<SpringApplication> testMockedConstruction
            = Mockito.mockConstruction(SpringApplication.class, (mock, context) -> {

                Mockito.when(mock.run(ArgumentMatchers.any(String[].class)))
                    .thenReturn(Mockito.mock(ConfigurableApplicationContext.class));

            })) {

            Gr001MarathonDiaryApiBootApplication.main(testArgs);

            /* 検証の準備 */
            final int               actualConstructionCount = testMockedConstruction.constructed().size();
            final SpringApplication actualApplication       = testMockedConstruction.constructed().get(0);

            /* 検証の実施 */
            Assertions.assertEquals(expectedConstructionCount, actualConstructionCount,
                "SpringApplication の生成回数が一致しません");
            Mockito.verify(actualApplication).setDefaultProperties(ArgumentMatchers.any(Properties.class));
            Mockito.verify(actualApplication).run(testArgs);

        }

    }

}
