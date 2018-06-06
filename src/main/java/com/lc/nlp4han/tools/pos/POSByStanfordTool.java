package com.lc.nlp4han.tools.pos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * 利用StanfordCoreNLP对生语料进行词性标注的工具
 * 
 * @author 刘小峰
 *
 */
public class POSByStanfordTool
{

	private static StanfordCoreNLP pipeline;

	private static String sep;

	private static List<String> getLines(File source, String encoding) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), encoding));
		String line = null;
		List<String> lines = new ArrayList<String>();

		while ((line = in.readLine()) != null)
		{
			lines.add(line);
		}

		in.close();

		return lines;
	}

	private static void tag(File in, File out, String encoding) throws IOException
	{
		System.out.println("标注文件" + in + "到" + out + "...");
		
		List<String> lines = getLines(in, encoding);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out), encoding));

		for (String text : lines)
		{
			text = text.trim();
			if (text.length() == 0)
				continue;
			
			CoreDocument document = new CoreDocument(text);

			pipeline.annotate(document);

			List<CoreSentence> sentences = document.sentences();

			for (CoreSentence sentence : sentences)
			{
				List<CoreLabel> tokens = sentence.tokens();
				List<String> tags = sentence.posTags();

				for (int i = 0; i < tokens.size(); i++)
				{
					bw.append(tokens.get(i).word() + sep + tags.get(i));

					if (i != tokens.size() - 1)
						bw.append("  ");
				}

				bw.append("\n");
			}
		}

		bw.close();
	}

	private static void initCoreNLP() throws IOException
	{
		Properties props = new Properties();
		props.load(POSByStanfordTool.class.getClassLoader().getResourceAsStream("StanfordCoreNLP-chinese.properties"));
		props.setProperty("annotators", "tokenize,ssplit,pos");
		pipeline = new StanfordCoreNLP(props);
	}

	public static void main(String[] args) throws IOException
	{
		initCoreNLP();

		String encoding = "GBK";
		sep = "/";
		File in = null;
		File out = null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-in"))
			{
				in = new File(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
			else if (args[i].equals("-out"))
			{
				out = new File(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-sep"))
			{
				sep = args[i + 1];
				i++;
			}
		}

		if (in.isFile() && !out.exists()) // 标注单个文件
			tag(in, out, encoding);
		else if(in.isDirectory() && out.exists()) // 标注整个目录下的文件
		{
			File[] rawFiles = in.listFiles();
			
			for(File raw : rawFiles)
			{
				String fileName = raw.getName();
				File outFile = new File(out, fileName + ".pos");
				
				tag(raw, outFile, encoding);
			}
		}
		else
		{
			System.out.println("文件参数错误。");
		}

	}

}
