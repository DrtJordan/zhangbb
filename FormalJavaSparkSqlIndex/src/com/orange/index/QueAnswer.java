package com.orange.index;

import java.io.Serializable;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import com.orange.utils.Constants;

public class QueAnswer implements Serializable{

	private static final long serialVersionUID = 1L;

	public void getQqueAnswer(SparkSession sparkHDFS) {
		
		sparkHDFS.read().jdbc(Constants.JDBC_EXIAOXIN, Constants.T_PLAT_QUE, Constants.JdbcCon()).createOrReplaceTempView("t_plat_que");
		sparkHDFS.sql(" select date,count(s_content) as que_count "
				+ " from(SELECT s_content , SUBSTR(s_create_time,1,10) AS DATE "
				+ " FROM t_plat_que "
				+ " WHERE s_create_time >= FROM_UNIXTIME(UNIX_TIMESTAMP()-86400,'yyyy-MM-dd 00:00:00') and s_create_time <= FROM_UNIXTIME(UNIX_TIMESTAMP(),'yyyy-MM-dd 00:00:00')) t "
				+ " group by date")
				.createOrReplaceTempView("t_que");
		sparkHDFS.read().jdbc(Constants.JDBC_EXIAOXIN, Constants.T_PLAT_ANSWER, Constants.JdbcCon()).createOrReplaceTempView("t_plat_answer");;
		sparkHDFS.sql(" select date,count(s_content) as answer_count "
				+ " from(SELECT s_content , SUBSTR(s_create_time,1,10) AS DATE "
				+ " FROM t_plat_answer "
				+ " WHERE s_create_time >= FROM_UNIXTIME(UNIX_TIMESTAMP()-86400,'yyyy-MM-dd 00:00:00') and s_create_time <= FROM_UNIXTIME(UNIX_TIMESTAMP(),'yyyy-MM-dd 00:00:00')) t "
				+ " group by date")
				.createOrReplaceTempView("t_ans");
		Dataset<Row> resultDSet = sparkHDFS.sql("select a.que_count s_que_count,b.answer_count s_answer_count ,a.date s_date, FROM_UNIXTIME(UNIX_TIMESTAMP() ,'yyyy-MM-dd HH:mm:ss') s_create_time "
				+ " from t_que a,t_ans b where a.date=b.date");
		resultDSet.write().mode("append").jdbc(Constants.JDBC_EXIAOXIN, Constants.T_QUE_ANSER, Constants.JdbcCon());
		
		
	}

}
