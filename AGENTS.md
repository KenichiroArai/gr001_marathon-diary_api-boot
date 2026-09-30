# AGENTS.md — marathon-diary api-boot

AI コーディングエージェント向けの作業ガイド。
Cursor / Codex / Claude Code など複数ツールで共通利用する。

## プロジェクト概要

- **役割**: マラソン日記の REST API の起動モジュール（組み立て専用）
- **含むもの**: 起動クラス、`application.yml`（ポート / CORS / DB 接続先）、使用する db-xxx の選択（`pom.xml`）
- **含めないもの**: Controller / DTO（`gr001_marathon-diary_api`）、業務ロジック（`gr001_marathon-diary_domain`）、DB アクセス（`gr001_marathon-diary_db-xxx`）

依存関係:

```text
api-boot ──> api ──> domain
   └──────> db-postgresql ──> domain
```

- api と db-xxx はお互いに依存しない。組み立てを知っているのは本モジュールだけ
- db-xxx の Bean は db-xxx 側の自動設定（`AutoConfiguration.imports`）で登録されるため、起動クラスで db のパッケージを指定しない

## 技術スタック

- 言語 / フレームワーク: Java 25 / Spring Boot 4.1.1（Spring MVC）
- ビルド / パッケージ管理: Maven（`spring-boot-starter-parent` 4.1.1、実行可能 jar）
- テスト: JUnit 5 / Spring Boot Test / JaCoCo 0.8.14（行・分岐 100%）

## ディレクトリ構成

```text
src/main/java/kmg/gr/gr001/api/boot/
  Gr001MarathonDiaryApiBootApplication.java   # 起動クラス
src/main/resources/
  application.yml                             # ポート / CORS / DB 接続先
src/test/java/kmg/gr/gr001/api/boot/
```

## ビルド・テスト

```bash
# 事前に domain → db-postgresql → api の順に mvn install しておく

# 起動:
mvn spring-boot:run

# テスト（JaCoCo レポート + カバレッジ 100% チェック。DB の起動は不要）:
mvn test

# パッケージ（実行可能 jar）:
mvn package
```

- カバレッジレポート: `target/site/jacoco/index.html`
- Eclipse 共有用実行データ: `target/jacoco.exec`
- 行 / 分岐カバレッジが 100% 未満だと `mvn test` は失敗する

## 作業時の原則

- 本モジュールには業務ロジック・Controller・DB アクセスを書かない
- DB の切り替えは `pom.xml` の db-xxx の dependency と `GR001_DB_URL` の変更だけで完結させる
- 接続情報は環境変数で渡し、シークレットをコミットしない
- 本ドキュメントのコーディングルール・テストルール・Javadoc ルールに従う

## 共通のコーディングルール

### メソッドの戻り値

- メソッドの戻り値は変数 `result` で定義する
- メソッドの戻り値の変数は先頭で宣言する
- return 文は `return result;` に統一する

### 処理コメント

- 機能ごと、処理のまとまり単位に `/* コメント */` で記載する
- 通常コメントは `//` で記載する

### Javadoc

- 修飾子に限らず必須
- 後述の「Javadoc のフォーマットルール」に従う

### 早期リターンパターン

- 早期リターン（ガード節）を使用し、不要なネストを避ける
- 条件が満たされない場合は早期に `return` する
- if-else の代わりにガード節を使い、インデントの深さを最小限に抑える

```java
// 望ましくない形式:
if (condition) {
    // 処理A
    // 処理B
}

// 望ましい形式:
if (!condition) {
    return result;
}
// 処理A
// 処理B
```

```java
public boolean someMethod(String input) {
    boolean result = false;  // 先頭で戻り値変数を宣言

    // 早期リターン（ガード節）
    if (input == null) {
        return result;
    }

    // メインの処理
    result = true;

    return result;  // 統一された形式で return
}
```

## テストのコーディングルール

### テスト単位

- メソッド単位で行い、`private` / `protected` / デフォルト / `public` すべて対象とする
- private メソッド・private 変数へのアクセスは `kmg.core.infrastructure.model.impl.KmgReflectionModelImpl` を使用する

### テストクラスのアノテーション

```java
@SuppressWarnings({
    "nls", "static-method"
})
```

### テストメソッド名

- `testXxx_パターンYyy` の形式とする
- 「Xxx」の先頭は大文字で対象メソッド名を入れる
- 「パターン」は正常系 `normal`、準正常系 `semi`、異常系 `error` とする
- 「Yyy」の先頭は大文字でテスト項目を入れる
- 例: `testXxx_normalYyy` / `testXxx_semiYyy` / `testXxx_errorYyy`

### テストメソッドのアクセス修飾子

- `testXXX` メソッドのアクセス修飾子はすべて `public` にする

### テストメソッドの中身

- 対象メソッドごとに行い、1 つのテストメソッドに 1 つのテストを実装する
- 正常系・準正常系・異常系に分けて実装する
  - 正常系: 正常処理が完了するパターン（正常に return され、throw されない）
  - 準正常系: 処理が正しく完了しないパターン（引数不正などにより return または throw）
  - 異常系: 正常系・準正常系以外の想定外パターン（DB 接続エラーなどにより throw）

### テストメソッドの Javadoc

- フォーマット: `対象メソッド名 メソッドのテスト - パターン:テスト内容`
- パターンには正常系・準正常系・異常系を入れる

```java
/**
 * targetMethod メソッドのテスト - 正常系:引数が1文字の場合
 */
```

### テストコードの実装順序

1. **期待値の定義** — `/* 期待値の定義 */`、`expected` で始まる変数
2. **準備** — `/* 準備 */`、`test` で始まる変数
3. **テスト対象の実行** — `/* テスト対象の実行 */`、`test` で始まる変数
4. **検証の準備** — `/* 検証の準備 */`、`actual` で始まる変数
5. **検証の実施** — `/* 検証の実施 */`
   - `Assertions.assertTrue` / `assertFalse` / `assertEquals` は `actualXXX` と説明を記載する
   - `Assertions.assertEquals` は `expectedXXX` と `actualXXX` と説明を記載する

### 検証方法の指定

- 1 行ずつ検証する
- `Assertions.assertTrue` / `assertFalse` は、`Assertions.assertEquals` で代行できる場合は代行する。ただし `condition` が `boolean` なら `assertTrue` / `assertFalse` を使用する
- `KmgMsgException` とその継承クラスは `kmg.core.infrastructure.test#verifyKmgMsgException(KmgMsgException, Class<?>, String, KmgComGenMsgTypes)` を使える場合は利用する
- `isInstance` / `instanceof` の比較は `Assertions.assertInstanceOf` を使用する
- null チェックは `Assertions.assertNull` を使用する
- それ以外は `Assertions.assertEquals` を使用し、期待値は「期待値の定義」の値を使う

### メッセージの検証

- メッセージは 1 行ずつ検証する

```java
@Test
public void testMethod() {

    /* 期待値の定義 */
    final String[] expectedMsgs = {
            "メッセージ1",
            "メッセージ2",
            "メッセージ3",
    };
    /* 準備 */

    /* テスト対象の実行 */

    /* 検証の準備 */
    final String[] actualMsgs = this.listAppender.list.stream().map(ILoggingEvent::getMessage)
        .toArray(String[]::new);

    /* 検証の実施 */

    // ログのチェック
    final int verMsgLength = Math.min(expectedMsgs.length, actualMsgs.length);

    for (int i = 0; i < verMsgLength; i++) {

        Assertions.assertEquals(expectedMsgs[i], actualMsgs[i],
            String.format("メッセージが一致しません: %s", expectedMsgs[i]));

    }

    // ログの数のチェック
    Assertions.assertEquals(expectedMsgs.length, actualMsgs.length);

}
```

## Javadoc のフォーマットルール

### 基本形式

```java
/**
 * クラスの説明をここに書きます。
 * 複数行の説明の場合は、このように記述します。
 *
 * @author 作成者名
 * @version バージョン番号
 * @since いつからこのクラスが存在するか（例：JDK1.8）
 */
public class SampleClass {

    /**
     * フィールドの説明をここに書きます。
     */
    private String field;

    /**
     * メソッドの説明をここに書きます。
     * 処理の詳細や目的を記述します。
     *
     * @param param1 最初のパラメータの説明
     * @param param2 2番目のパラメータの説明
     * @return 戻り値の説明
     * @throws Exception1 例外が発生する条件の説明
     * @throws Exception2 別の例外が発生する条件の説明
     * @see 関連するクラスやメソッドへの参照
     * @deprecated 非推奨となった場合の説明（該当する場合）
     */
    public String sampleMethod(String param1, int param2) throws Exception {
        // メソッドの実装
    }
}
```

### 主要なタグ

| タグ | 用途 |
| --- | --- |
| `@param` | メソッドのパラメータの説明 |
| `@return` | 戻り値の説明 |
| `@throws` | 発生する可能性のある例外の説明 |
| `@author` | 作成者 |
| `@version` | バージョン情報 |
| `@since` | 導入されたバージョン |
| `@see` | 関連する他のクラスやメソッドへの参照 |
| `@deprecated` | 非推奨であることを示す |
| `@link` | 他のクラスやメソッドへのリンク |
| `@code` | コードの例を示す |
| `@value` | 定数値を参照する |
| `@serial` | シリアライズに関する情報 |

### 記述ガイドライン

- 最初の文は要約文として簡潔に書く
- 完全な文章で、技術的に正確に記述する
- 必要な情報を漏れなく記載し、HTML タグを適切に使う

コード例:

```java
/**
 * サンプルコードの使用例：
 * <pre>
 * {@code
 *     String result = obj.sampleMethod("test", 123);
 * }
 * </pre>
 */
```

リンク:

```java
/**
 * 詳細は{@link OtherClass#otherMethod()}を参照してください。
 */
```

箇条書き:

```java
/**
 * このメソッドは以下の処理を行います：
 * <ul>
 * <li>データの検証</li>
 * <li>データの変換</li>
 * <li>結果の保存</li>
 * </ul>
 */
```

### チーム統一フォーマット例

```java
/**
 * [クラス/メソッド/フィールドの名前]の説明
 *
 * 詳細な説明（必要な場合）
 *
 * 業務ロジックの説明（必要な場合）
 *
 * @author      作成者 <email@example.com>
 * @param       [引数名] [引数の説明]
 * @return      [戻り値の説明]
 * @throws      [例外クラス名] [例外の発生条件]
 * @see         [参照すべき他のクラスやメソッド]
 * @since       [追加されたバージョン]
 * @version     [現在のバージョン]
 * @deprecated  [非推奨となった理由と代替手段]（該当する場合）
 */
```

## 変更時のチェックリスト

- [ ] 依存の向き（api-boot 以外が db-xxx を参照していない）の確認
- [ ] テストの追加 / 更新（命名・実装順序・検証方法を含む）
- [ ] `mvn test` で JaCoCo カバレッジ 100% を維持
- [ ] コーディングルール（戻り値 `result`、早期リターン、処理コメント）の順守
- [ ] Javadoc の追加 / 更新

## やってはいけないこと

- 本モジュールに Controller / 業務ロジック / DB アクセスを追加すること
- シークレットをコードやログに出すこと
- 深いネストのままガード節を使わずに実装すること
- テストメソッドに複数ケースを詰め込むこと
- （追記）

## 関連リポジトリ

- 仕様: `kb001_marathon-diary_doc`
- API（Controller / DTO）: `gr001_marathon-diary_api`
- ドメイン: `gr001_marathon-diary_domain`
- DB: `gr001_marathon-diary_db-postgresql`
- Web（Next.js）: `mk001_marathon-diary_web-next`
- Web（Vue・参考）: `mk001_marathon-diary_web-vue`
- 基盤: `kmg-core` / `kmg-fund`

## 参考リンク

- README: `./README.md`

## 順守

以上の内容を順守し、タスクを遂行してください。
