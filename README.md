# marathon-diary api-boot

マラソン日記の REST API の起動モジュールである。

## 概要

本リポジトリは、マラソン日記（marathon-diary）の REST API を起動するための組み立て専用モジュールである。
Controller・業務ロジック・DB アクセスは持たず、次のモジュールを組み合わせて実行可能 jar にする。

```text
api-boot ──> api（Controller / DTO） ──> domain（業務ロジック / Repository インタフェース）
   └──────> db-postgresql（Repository 実装 / DBFlute） ──> domain
```

- api と db-xxx はお互いの存在を知らない。どの DB を使うかを知っているのは本モジュールだけ
- db-xxx の Bean は db-xxx 側の自動設定で登録されるため、起動クラスで db のパッケージを指定しない

技術スタック:

- Java 25 / Spring Boot 4.1.1
- Maven（実行可能 jar）

## 必要環境

- JDK 25
- Maven 3.6.3 以降
- Docker（DB を起動する場合）
- kmg-core / kmg-fund（ローカル `mvn install` または GitHub Packages）

## ビルド手順

ルートに親 pom が無いため、依存先から順にローカルリポジトリへ `mvn install` する。

```bash
# 1. ドメイン
cd ../gr001_marathon-diary_domain && mvn install

# 2. DB 実装（初回は DB 起動とコード生成が必要。手順は db-postgresql の README を参照）
cd ../gr001_marathon-diary_db-postgresql && mvn install

# 3. API（Controller / DTO）
cd ../gr001_marathon-diary_api && mvn install

# 4. 本モジュール
cd ../gr001_marathon-diary_api-boot && mvn install
```

テスト（`mvn test`）は DB を起動しなくても実行できる（DB への接続はリクエスト時に行われるため）。

## 起動手順

```bash
# 1. DB を起動する（db-postgresql で実行）
cd ../gr001_marathon-diary_db-postgresql && docker compose up -d

# 2. API を起動する（本モジュールで実行）
cd ../gr001_marathon-diary_api-boot && mvn spring-boot:run
# または: java -jar target/gr001_marathon-diary_api-boot-0.1.0.jar
```

動作確認:

```text
GET http://localhost:8080/api/health   -> {"status":"UP","service":"marathon-diary-api"}
GET http://localhost:8080/api/sample   -> {"message":"Hello from sample database"}
```

`/api/sample` のメッセージは DB の `sample_greeting` テーブルから取得している（DB → domain → api → 画面の配線確認用サンプル）。

## 設定

`src/main/resources/application.yml` で設定する。DB 接続先は環境変数で上書きできる。

| 環境変数 | 既定値 | 内容 |
| --- | --- | --- |
| `GR001_DB_URL` | `jdbc:postgresql://localhost:5432/marathondiary` | JDBC URL |
| `GR001_DB_USERNAME` | `marathondiary` | DB ユーザ |
| `GR001_DB_PASSWORD` | `marathondiary` | DB パスワード |

開発用 CORS（`app.cors.allowed-origins`）は `http://localhost:3000` / `http://127.0.0.1:3000` を許可している（Next.js 開発サーバ向け）。別のフロントを追加する場合は同リストにオリジンを追加する。

## DB の切り替え方法

例として PostgreSQL から MySQL に切り替える場合の手順を示す。api / domain は変更しない。

1. `gr001_marathon-diary_db-mysql` を作成する
   - db-postgresql と同じ構成で、domain の Repository インタフェースを実装する
   - `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` に自動設定クラスを登録する
   - `mvn install` する
2. 本モジュールの `pom.xml` の DB 実装の dependency を差し替える

   ```xml
   <dependency>
       <groupId>kmg.gr.gr001</groupId>
       <artifactId>gr001_marathon-diary_db-mysql</artifactId>
       <version>${gr001.version}</version>
   </dependency>
   ```

3. 接続先を環境変数で指定して起動する

   ```bash
   GR001_DB_URL=jdbc:mysql://localhost:3306/marathondiary mvn spring-boot:run
   ```

## バッチなど別の起動形態を追加する場合

本モジュールと同じ考え方で、`batch`（ジョブ定義）と `batch-boot`（起動モジュール）の組を作る。
`api` と `api-boot` の関係と同じ形になる。

```text
batch-boot ──> batch（ジョブ定義） ──> domain
    └────────> db-postgresql ──> domain
```

batch 本体は domain だけに依存し、どの DB を使うかは batch-boot の `pom.xml` で決める。

## Eclipse の設定

Eclipse のビルド・パス（`.classpath`）は Git で管理している。
内容は m2e が `pom.xml` から生成する標準の構成（JRE は `JavaSE-25`、出力先は `target/classes` / `target/test-classes`）で、PC 固有の絶対パスは含まない。
`.settings/` と `bin/` は Git で管理しない。

- `src/test/resources` は現在存在しないが、`.classpath` では任意（optional）のソースフォルダのため Eclipse のエラーにはならない
- `.classpath` は手で編集しない。`pom.xml` を変更した場合は「Maven」→「プロジェクトの更新」で反映する
- 「プロジェクトの更新」の実行後は `.classpath` に差分が出ていないことを確認する（差分が出た場合は意図した変更か確認してからコミットする）

## ディレクトリ構成

```text
src/main/java/kmg/gr/gr001/api/boot/
  Gr001MarathonDiaryApiBootApplication.java   # 起動クラス
src/main/resources/
  application.yml                             # ポート / CORS / DB 接続先
src/test/java/                                # テスト
```

## ライセンス

[MIT License](./LICENSE)
