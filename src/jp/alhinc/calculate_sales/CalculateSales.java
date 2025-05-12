package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String SALES_FILE_INVALID_FORMAT = "売上ファイル名が連番になっていません";
	private static final String EXCEEDED_10_DIGITS = "合計金額が10桁を超えました";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		if (args.length != 1) {
			//コマンドライン引数が1つ設定されていなかった場合は、
			//エラーメッセージをコンソールに表示します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// 売り上げファイル集計処理
		if (!tallySalesFile(args[0], branchNames, branchSales)) {
			return;
		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			if (!file.exists()) {
				//支店定義ファイルが存在しない場合、コンソールにエラーメッセージを表示します。
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(","); // 1行ずつ読み込んだデータをカンマで区切って配列に入れる

				if ((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))) {
					//支店定義ファイルの仕様が満たされていない場合、
					//エラーメッセージをコンソールに表示します。
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				branchNames.put(items[0], items[1]); // 支店コードと支店名を保持するMapに値を追加
				branchSales.put(items[0], 0L); // 支店コードと売上金額を保持するMapに支店コードのみ追加(金額は後工程)
			}

			// ★テスト用
			// System.out.println(branchNames);
			// System.out.println(branchSales);
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 売り上げファイル集計処理
	 *
	 * @param フォルダパス
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean tallySalesFile(String path, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;
		File files[] = new File(path).listFiles(); // ディレクトリ内のファイル名を取得
		List<File> rcdFiles = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {
			// 対象がファイルであり、「数字8桁.rcd」なのか判定します。
			if (files[i].isFile() && files[i].getName().matches("^[0-9]{8}\\.rcd$")) {
				//trueの場合の処理
				rcdFiles.add(files[i]);
			}
		}

		//比較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
		for (int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換します。
			if ((latter - former) != 1) {
				//2つのファイル名の数字を比較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表示します。
				System.out.println(SALES_FILE_INVALID_FORMAT);
				return false;
			}
		}

		List<String> items = new ArrayList<>();
		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {
			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);

				String line;
				List<String> lines = new ArrayList<>();
				// 一行ずつ読み込む
				while ((line = br.readLine()) != null) {
					items.add(line); // 1行ずつ読み込んだデータをリストに入れる
					lines.add(line); // エラー処理用リストに入れる
				}

				if (!branchSales.containsKey(lines.get(0))) {
					//支店情報を保持しているMapに売上ファイルの支店コードが存在しなかった場合は、
					//エラーメッセージをコンソールに表示します。
					System.out.println(rcdFiles.get(i).getName() + "の支店コードが不正です");
					return false;
				}

				if (lines.size() != 2) {
					//売上ファイルの行数が2行ではなかった場合は、
					//エラーメッセージをコンソールに表示します。
					System.out.println(rcdFiles.get(i).getName() + "のフォーマットが不正です");
					return false;
				}

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return false;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return false;
					}
				}
			}
		}

		// ★テスト用
		//System.out.println(items + " : items 支店コードと売上金額の抽出リスト");

		String strStoreCode = null;

		for (int i = 0; i < items.size(); i++) {
			long storeSale = 0;

			if (i % 2 == 0) {
				strStoreCode = items.get(i);
			} else if (i % 2 == 1) {
				if (!items.get(i).matches("^[0-9]{1,}$")) {
					//売上金額が数字ではなかった場合は、
					//エラーメッセージをコンソールに表示します。
					System.out.println(UNKNOWN_ERROR);
					return false;
				}

				// 売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				storeSale = Long.parseLong(items.get(i));

			}

			// 読み込んだ売上金額を加算します。
			Long saleAmount = branchSales.get(strStoreCode) + storeSale;

			if (saleAmount >= 10000000000L) {
				// 売上金額が11桁以上の場合、エラーメッセージをコンソールに表示します。
				System.out.println(EXCEEDED_10_DIGITS);
				return false;
			}

			// 加算した売上金額をMapに追加します。
			branchSales.put(strStoreCode, saleAmount);
		}

		// ★テスト用
		// System.out.println(branchSales + " : branchSales 支店コードと売上金額を保持するMap");
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for文で繰り返されているので、1つ目のキーが取得できたら、
				//2つ目の取得...といったように、次々とkeyという変数に上書きされていきます。
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();

				// ★テスト用
				// System.out.println(key + "," + branchNames.get(key) + "," + branchSales.get(key));
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}

		return true;
	}

}
