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

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// 売り上げファイル集計処理
		if(!tallySalesFile(args[0], branchNames, branchSales)) {
			return;
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
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
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");			// 1行ずつ読み込んだデータをカンマで区切って配列に入れる
				branchNames.put(items[0], items[1]);		// 支店コードと支店名を保持するMapに値を追加
				branchSales.put(items[0], 0L);				// 支店コードと売上金額を保持するMapに支店コードのみ追加(金額は後工程)
			}

		// ★テスト用
		// System.out.println(branchNames);
		// System.out.println(branchSales);
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
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
		Map<String, List<String>> rtObjet = new HashMap<>();	 // 読み込み処理の結果とデータを入れる
		String rtRead = null;									 // 読み込み処理の結果
		rtObjet = readSalesFile(path);							 // 売り上げファイル読み込み処理

		for (String key : rtObjet.keySet()) {
			rtRead = key;
			if(!Boolean.valueOf(key)) {
				return false;		// 読み込み処理の結果がfalseならリターン
			}
		}

		List<String> salesData = new ArrayList<>();
		salesData = rtObjet.get(rtRead);		// 読み込んだ売り上げデータを取得

		String strStoreCode = null;

		for (int i = 0; i < salesData.size(); i++) {
			long storeSale = 0;

			if(i % 2 == 0) {
				strStoreCode = salesData.get(i);
			}else if(i % 2 == 1) {
				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				storeSale = Long.parseLong(salesData.get(i));
			}

			//読み込んだ売上金額を加算します。
			Long saleAmount = branchSales.get(strStoreCode) + storeSale;
			//加算した売上⾦額をMapに追加します。
			branchSales.put(strStoreCode, saleAmount);

			// ★テスト用
			// System.out.println(strStoreCode + " : strStoreCode," + saleAmount + ":saleAmount");

		}

		// ★テスト用
		// System.out.println(branchSales + " : branchSales 支店コードと売上金額を保持するMap");
		return true;
	}

	/**
	 * 売り上げファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @return 読み込み可否、売り上げデータ
	 */
	private static Map<String, List<String>> readSalesFile(String path)  {
		String rtRead = "true";

		BufferedReader br = null;
		File files[] = new File(path).listFiles();	// ディレクトリ内のファイル名を取得
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			//matches を使用してファイル名が「数字8桁.rcd」なのか判定します。
			if(files[i].getName().matches("[0-9]{1,8}+.rcd")) {
			    //trueの場合の処理
				//System.out.println(files[i].getName());
				rcdFiles.add(new File(path, files[i].getName()));
			}
		}

		List<String> items = new ArrayList<>();

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for(int i = 0; i < rcdFiles.size(); i++) {

			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			try {

				File file = new File(path,rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;

				// 一行ずつ読み込む
				while((line = br.readLine()) != null) {

					items.add(line);			// 1行ずつ読み込んだデータをリストに入れる
					// System.out.println(items);	// ★テスト
				}

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				rtRead = "false";
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						rtRead = "false";
					}
				}
			}

		}

		Map<String, List<String>> rtObject = new HashMap<>();

		rtObject.put(rtRead, items);
		// System.out.println(rtObject);		// ★テスト
		return rtObject;
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
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
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
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}

		return true;
	}

}
