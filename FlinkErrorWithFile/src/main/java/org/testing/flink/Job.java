package org.testing.flink;

import java.util.List;
import java.util.ArrayList;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.DiscardingOutputFormat;
import org.apache.flink.api.java.operators.JoinOperator;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.common.functions.JoinFunction;

public class Job
{
	private ExecutionEnvironment _env;

	public static void main(String[] args)
		throws Exception
	{
		Job job = new Job();
		job.run();
	}

	public Job()
	{
		_env = ExecutionEnvironment.getExecutionEnvironment();
	}

	public DataSet<Tuple3<Integer,Integer,Integer>> getDataSet(String file)
	{
		return _env
			.readCsvFile(file)
			.types(Integer.class,Integer.class,Integer.class);
	}

	public DataSet<Tuple3<Integer,Integer,Integer>> getDataSetA()
	{
		ArrayList<Tuple3<Integer,Integer,Integer>> listA =
			new ArrayList<Tuple3<Integer,Integer,Integer>>();
		for(int ii = 0 ; ii < 5 ; ii++) {
			listA.add(new Tuple3<Integer,Integer,Integer>(ii, ii, ii));
		}

		return _env.fromCollection(listA);
	}

	public DataSet<Tuple3<Integer,Integer,Integer>> getDataSetB()
	{
		ArrayList<Tuple3<Integer,Integer,Integer>> listB =
			new ArrayList<Tuple3<Integer,Integer,Integer>>();

		listB.add(new Tuple3<Integer,Integer,Integer>(10000,10000,4));
		listB.add(new Tuple3<Integer,Integer,Integer>(10000,2,4));
		listB.add(new Tuple3<Integer,Integer,Integer>(10000,5633,4));
		listB.add(new Tuple3<Integer,Integer,Integer>(10000,6613,4));
		listB.add(new Tuple3<Integer,Integer,Integer>(10000,8293,4));
		listB.add(new Tuple3<Integer,Integer,Integer>(10001,10001,3));
		listB.add(new Tuple3<Integer,Integer,Integer>(10001,2,3));
		listB.add(new Tuple3<Integer,Integer,Integer>(10001,5660,3));
		listB.add(new Tuple3<Integer,Integer,Integer>(10001,5874,3));
		listB.add(new Tuple3<Integer,Integer,Integer>(10002,10001,4));

		return _env.fromCollection(listB);
	}


	public void run() throws Exception
	{
		DataSet<Tuple3<Integer,Integer,Integer>> setA = getDataSetA();

		// Not sure, but when the dataset is loaded from a collection,
		// the process works; however, when it is loaded from a file
		// it is not.

		//DataSet<Tuple3<Integer,Integer,Integer>> setB = getDataSetB();
		DataSet<Tuple3<Integer,Integer,Integer>> setB = getDataSet("file:///tmp/nodes.csv");

		DataSet<Tuple3<Integer,Integer,Integer>> expansion = setB.join(setA)
			.where("f2").equalTo("f1")
			.with(new SimpleJoin3Function());

		expansion.output(new DiscardingOutputFormat());

		_env.execute();
	}

	public static class SimpleJoin3Function
		implements JoinFunction<
				   Tuple3<Integer,Integer,Integer>,
				   Tuple3<Integer,Integer,Integer>,
				   Tuple3<Integer,Integer,Integer>>
	{
		@Override
		public Tuple3<Integer,Integer,Integer> join(
			Tuple3<Integer,Integer,Integer> first,
			Tuple3<Integer,Integer,Integer> second)
		{
			return new Tuple3<Integer,Integer,Integer>(
				second.f0,
				first.f1,
				first.f0
			);
		}
	}
}
