package com.lc.nlp4han.tools.pst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.ColorUIResource;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

/**
 * 短语结构树标注工具
 * 
 * 可以利用StanfordCoreNLP生成初步标注结果
 * 
 *
 */
public class TreeEditorTool
{
	private JFrame frame = new JFrame("未命名.txt" + "[ " + 1 + " / " + 1 + " ]");

	private TreePanel treePanel = new TreePanel();
	private JScrollPane paintjsp;
	private JPanel editPanel = new JPanel();

	private JTextArea editRawText = new JTextArea(2, 80);
	private JTextArea editBracket = new JTextArea(8, 80);

	private JButton toTreeButtion = new JButton("生成结构树");
	private JButton outputButton = new JButton("导出到文件");
	private JButton rePaintButton = new JButton("重画结构树");
	private JButton updateBracketButton = new JButton("更新表达式");
	private JButton parseButton = new JButton("句法分析");

	private JFileChooser fileChooser = new JFileChooser();
	private TxtFileFilter txtFileFilter = new TxtFileFilter();

	private static String charsetName = "gbk";// 默认读取文本编码为gbk

	private Vector<TreePanelNode> nodes = new Vector<TreePanelNode>();
	private ArrayList<TreePanelNode> treeLists = new ArrayList<TreePanelNode>();

	private HashMap<String, Boolean> hasModeified = new HashMap<String, Boolean>(); // 记录文本是否被改变

	private TreeAtTxt treeAtTxt = new TreeAtTxt();// 表示当前面板上的括号表达式
	private ArrayList<TreeAtTxt> allTreesAtTxt = new ArrayList<TreeAtTxt>();// 表示所有文件中的括号表达式

	private static int RIGHT = 0, LEFT = -1;// 表示左右滑动

	private StanfordCoreNLP pipeline;
//	private Annotation annotation;

	public void init()
	{
		coreNLPInit();
		
		treeAtTxt = new TreeAtTxt(treeLists);
		allTreesAtTxt.add(treeAtTxt);
		hasModeified.put(null, Boolean.FALSE);
		treePanel.setHasModeified(hasModeified);
		treePanel.setTreeAtTxt(treeAtTxt);
		treePanel.setNodes(nodes);
		treePanel.setTreeLists(treeLists);

		treePanel.setPreferredSize(new Dimension(10000, 10000));
		paintjsp = new JScrollPane(treePanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(parseButton);
		buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(toTreeButtion);
		buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.add(rePaintButton);
		buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.add(updateBracketButton);
		buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.add(outputButton);

		// 创建菜单
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("文件");
		JMenuItem jmi_new = new JMenuItem("新建");
		JMenuItem jmi_open = new JMenuItem("打开...");
		JMenuItem jmi_save = new JMenuItem("保存");
		JMenuItem jmi_saveAs = new JMenuItem("另存为...");
		
		JMenuItem jmiExit = new JMenuItem("退出");

		JMenu menuCharset = new JMenu("编码");
		JMenuItem jmi_gbk = new JMenuItem("gbk");
		JMenuItem jmi_utf_8 = new JMenuItem("utf-8");
		jmi_gbk.setBackground(Color.GREEN);
		menuFile.add(jmi_new);
		menuFile.add(jmi_open);
		menuFile.add(jmi_save);
		menuFile.add(jmi_saveAs);
		menuFile.add(jmiExit);
		
		menuCharset.add(jmi_gbk);
		menuCharset.add(jmi_utf_8);
		
		menuBar.add(menuFile);
		menuBar.add(menuCharset);

		JScrollPane editjsp0 = new JScrollPane(editRawText); // 输入未处理的句子
		JScrollPane editjsp = new JScrollPane(editBracket);// 输入括号表达式
		JPanel editTextPanel = new JPanel();
		editTextPanel.setLayout(new BorderLayout());
		editTextPanel.add(editjsp);
		editTextPanel.add(editjsp0, BorderLayout.NORTH);

		editPanel.setLayout(new BorderLayout());
		editPanel.add(editTextPanel);
		editPanel.add(buttonPanel, BorderLayout.EAST);

		JPanel functionPanel = new JPanel();
		functionPanel.setLayout(new BoxLayout(functionPanel, BoxLayout.Y_AXIS));
		JButton add = new JButton("增加");
		JButton delete = new JButton("删除");
		JButton combine = new JButton("连接");
		JButton selectRoot = new JButton("root");
		JButton clearPanel = new JButton("清空");
		JButton leftTree = new JButton("<——");
		JButton rightTree = new JButton("——>");

		functionPanel.add(add);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(delete);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(combine);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(selectRoot);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(clearPanel);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(leftTree);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(rightTree);

		treePanel.setAdd(add);
		treePanel.setDelete(delete);
		treePanel.setCombine(combine);
		treePanel.setSelectRoot(selectRoot);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(paintjsp);
		panel.add(functionPanel, BorderLayout.WEST);
		panel.add(editPanel, BorderLayout.NORTH);

		int width = 4 * Toolkit.getDefaultToolkit().getScreenSize().width / 5;
		int height = 4 * Toolkit.getDefaultToolkit().getScreenSize().height / 5;
		frame.setSize(width, height);// 设置大小
		frame.setLocationByPlatform(true);
		frame.add(panel);

		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// 新建菜单
		jmi_new.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				String currentTxtPath = treePanel.getTreeAtTxt().getTxtPath();
				if (!hasModeified.get(currentTxtPath).booleanValue()) // txtPath对应文件没有修改
				{
					// 新建
					resetTreePanel();
				}
				else // 需要保存
				{
					int returnValue = JOptionPane.YES_OPTION;
					returnValue = JOptionPane.showConfirmDialog(frame, "是否要保存,当前页面的内容。", "确认对话框",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{
						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (treePanel.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (treePanel.getTreeLists().get(0).examTheTree()
										&& treePanel.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();
											FileOutputStream fos = new FileOutputStream(currentTxtPath);
											OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
											BufferedWriter bw = new BufferedWriter(osw);
											for (String word : treePanel.getTreeLists().get(0).changeIntoText())
												bw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											treePanel.setHasModeified(hasModeified);
											treePanel.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null

											bw.close();

											// 新建
											resetTreePanel();

										}
										else
										{// 点击了取消或退出对话框，什么也不用做

										}

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}

								}
								else
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}

							}
							else
							{// 修改了打开的文件中的某棵树
								for (TreeAtTxt tat : allTreesAtTxt)
								{
									if (tat.getTxtPath().equals(currentTxtPath))
										treesOfSameTxt.add(tat.getTreeListWithOneTree().get(0));
								}
							}

							if (!treesOfSameTxt.isEmpty())
							{// 执行了上面的else
								boolean correctFormat = true;
								for (TreePanelNode treeRoot : treesOfSameTxt)
								{
									if (treeRoot.examTheTree() && treeRoot != null)
									{

									}
									else
									{
										correctFormat = false;
										break;
									}
								}
								if (correctFormat == false)
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileOutputStream fos = new FileOutputStream(currentTxtPath);
										OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
										BufferedWriter bw = new BufferedWriter(osw);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												bw.write(word);
											bw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										treePanel.setHasModeified(hasModeified);
										bw.close();
										// 新建
										resetTreePanel();

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}
								}

							}

						}
						else
						{// 当前面板不止一棵树/或者没有树
							JOptionPane.showMessageDialog(frame, "画板上只有一棵树时才能保存。", "消息提示框",
									JOptionPane.INFORMATION_MESSAGE);
						}

					}
					else if (returnValue == JOptionPane.NO_OPTION) // 确认不保存
					{
						// 新建
						resetTreePanel();
					}
					else // 点击了取消或者退出
					{
						resetButtonstatus();
						treePanel.initCombineNodes();
						treePanel.setSelectedNodes(-1);
						treePanel.repaint();
					}
				}
				
				resetButtonstatus();
				treePanel.initCombineNodes();
				treePanel.setSelectedNodes(-1);
				treePanel.repaint();
			}
		});

		// 打开菜单
		jmi_open.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				int returnValue = JOptionPane.YES_OPTION;
				String currentTxtPath = treePanel.getTreeAtTxt().getTxtPath();
				if (!hasModeified.get(currentTxtPath).booleanValue())
				{// txtPath对应文件没有修改
					// 打开
					try
					{
						openTxts();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				else
				{
					// TODO: 保存操作代码重复
					returnValue = JOptionPane.showConfirmDialog(frame, "是否要保存,当前页面的内容。", "确认对话框",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{

						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (treePanel.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (treePanel.getTreeLists().get(0).examTheTree()
										&& treePanel.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();

											FileOutputStream fos = new FileOutputStream(currentTxtPath);
											OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
											BufferedWriter bw = new BufferedWriter(osw);

											for (String word : treePanel.getTreeLists().get(0).changeIntoText())
												bw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											treePanel.setHasModeified(hasModeified);
											treePanel.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
											frame.setTitle(new File(currentTxtPath).getName() + "[ " + "1 / 1" + " ]");
											bw.close();

											// 打开
											try
											{
												openTxts();
											}
											catch (IOException e1)
											{
												e1.printStackTrace();
											}

										}
										else
										{// 点击了取消或退出对话框，什么也不用做

										}

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}

								}
								else
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}

							}
							else
							{// 修改了打开的文件中的某棵树
								for (TreeAtTxt tat : allTreesAtTxt)
								{
									if (tat.getTxtPath().equals(currentTxtPath))
										treesOfSameTxt.add(tat.getTreeListWithOneTree().get(0));
								}
							}

							if (!treesOfSameTxt.isEmpty())
							{// 执行了上面的else
								boolean correctFormat = true;
								for (TreePanelNode treeRoot : treesOfSameTxt)
								{
									if (treeRoot.examTheTree() && treeRoot != null)
									{

									}
									else
									{
										correctFormat = false;
										break;
									}
								}
								if (correctFormat == false)
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileOutputStream fow = new FileOutputStream(currentTxtPath);
										OutputStreamWriter osw = new OutputStreamWriter(fow, charsetName);
										BufferedWriter bw = new BufferedWriter(osw);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												bw.write(word);
											bw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										treePanel.setHasModeified(hasModeified);
										bw.close();
										// 打开
										try
										{
											openTxts();
										}
										catch (IOException e1)
										{
											e1.printStackTrace();
										}

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}
								}
							}
						}
						else
						{// 当前面板不止一棵树/或者没有树
							JOptionPane.showMessageDialog(frame, "画板上只有一棵树时才能保存。", "消息提示框",
									JOptionPane.INFORMATION_MESSAGE);
						}

					}
					else if (returnValue == JOptionPane.NO_OPTION)
					{
						// 打开
						try
						{
							openTxts();
						}
						catch (IOException e1)
						{
							e1.printStackTrace();
						}
					}
					else
					{// 点击了取消或者退出

					}
				}
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
			}
		});

		// 保存菜单
		jmi_save.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// TODO: 保存操作代码重复
				if (treePanel.getHasModeified().get(treePanel.getTreeAtTxt().getTxtPath()))
				{// 文件被修改过
					if (treePanel.getTreeLists().size() != 1)
					{
						JOptionPane.showMessageDialog(frame, "保存时画板必须只有一棵树", "消息提示框", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{// 要保存的面板中只有一棵树
						if (treePanel.getTreeLists().get(0).examTheTree() && treePanel.getTreeLists().get(0) != null)
						{// 格式正确
							if (treePanel.getTreeAtTxt().getTxtPath() == null)
							{// 还没有为当前文件设置路径和名称
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
								fileChooser.setSelectedFile(new File(sdf.format(new Date()) + ".txt"));
								if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
								{
									String currentTxtPath = fileChooser.getSelectedFile().toString();
									try
									{
										FileOutputStream fos = new FileOutputStream(currentTxtPath);
										OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
										BufferedWriter bw = new BufferedWriter(osw);
										for (String word : treePanel.getTreeLists().get(0).changeIntoText())
											bw.write(word);

										hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										treePanel.setHasModeified(hasModeified);
										treePanel.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
										resetButtonstatus();
										treePanel.setSelectedNodes(-1);
										treePanel.initCombineNodes();
										frame.setTitle(fileChooser.getSelectedFile().getName() + "[ " + "1 / 1" + " ]");
										treePanel.repaint();
										bw.close();
									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}
								}
							}
							else
							{// 该文件是打开已有的文本
								String currentTxtPath = treePanel.getTreeAtTxt().getTxtPath();
								ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
								for (TreeAtTxt tat : allTreesAtTxt)
								{
									if (tat.getTxtPath().equals(currentTxtPath))
										treesOfSameTxt.add(tat.getTreeListWithOneTree().get(0));
								}
								if (!treesOfSameTxt.isEmpty())
								{
									boolean correctFormat = true;
									for (TreePanelNode treeRoot : treesOfSameTxt)
									{// 检查树的格式是否正确
										if (treeRoot.examTheTree() && treeRoot != null)
										{

										}
										else
										{
											correctFormat = false;
											break;
										}
									}
									if (correctFormat == false)
									{
										JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
												JOptionPane.INFORMATION_MESSAGE);
									}
									else
									{
										try
										{

											FileOutputStream fos = new FileOutputStream(currentTxtPath);
											OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
											BufferedWriter bw = new BufferedWriter(osw);
											for (TreePanelNode treeRoot : treesOfSameTxt)
											{
												for (String word : treeRoot.changeIntoText())
													bw.write(word);
												bw.write("\r\n");
											}
											// 修改面板的部分状态
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											treePanel.setHasModeified(hasModeified);
											resetButtonstatus();
											treePanel.setSelectedNodes(-1);
											treePanel.initCombineNodes();
											treePanel.repaint();
											bw.close();

										}
										catch (IOException e1)
										{
											e1.printStackTrace();
										}
									}

								}
							}
						}
						else
						{// 格式不正确
							JOptionPane.showMessageDialog(frame, "树的格式不正确。", "树的格式不正确",
									JOptionPane.INFORMATION_MESSAGE);
							resetButtonstatus();
							treePanel.setSelectedNodes(-1);
							treePanel.initCombineNodes();
							treePanel.repaint();
						}
					}
				}
				else
				{// 文件没有被修改
					// 暂时认为不用管
					resetButtonstatus();
					treePanel.setSelectedNodes(-1);
					treePanel.initCombineNodes();
					treePanel.repaint();
				}
			}

		});

		// 另存为菜单
		jmi_saveAs.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				treeLists = treePanel.getTreeLists();
				nodes = TreePanelNode.nodesOfAllTrees(treeLists);
				int returnValue = JOptionPane.YES_OPTION;
				for (TreePanelNode root : treeLists)
				{
					if (root.examTheTree() && root != null)
					{// 判断森林中的树是否都符合格式
						System.out.println(returnValue);
					}
					else
					{

						returnValue = JOptionPane.showConfirmDialog(frame, "括号表达式格式有错误,是否要保存。", "确认对话框",
								JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

						break;
					}

				}

				if (returnValue == JOptionPane.YES_OPTION && treeLists != null && nodes != null && !treeLists.isEmpty()
						&& !nodes.isEmpty())
				{
					try
					{
						int count = 0;
						SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
						System.out.println(date.format(new Date()) + ".txt");
						fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
						if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
						{// 确定保存
							String filePath = fileChooser.getSelectedFile().toString();
							FileOutputStream fos = new FileOutputStream(filePath);
							OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
							BufferedWriter bw = new BufferedWriter(osw);
							count = 0;
							for (TreePanelNode root : treeLists)
							{
								count++;
								if (root.examTheTree() && root != null)
									for (String word : root.changeIntoText())
										bw.write(word);
								else
									bw.write("第" + count + "个括号表达式格式错误。");

								bw.write("\r\n");
							}
							bw.close();

						}
						resetButtonstatus();
						treePanel.initCombineNodes();
						treePanel.setSelectedNodes(-1);
						treePanel.repaint();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		
		// 退出菜单
		jmiExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});

		jmi_gbk.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				charsetName = "gbk";
				jmi_utf_8.setBackground((Color) new ColorUIResource(238, 238, 238));
				jmi_gbk.setBackground(Color.green);
			}
		});
		
		jmi_utf_8.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				charsetName = "utf-8";
				jmi_gbk.setBackground((Color) new ColorUIResource(238, 238, 238));
				jmi_utf_8.setBackground(Color.GREEN);
			}
		});

		// 句法解析按钮
		parseButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				String text = editRawText.getText();
				if (text.trim().length() != 0)
				{// 将无格式的括号表达式转化为有格式的表达式
					Annotation annotation = new Annotation(text);
					pipeline.annotate(annotation);
					List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

					ArrayList<TreePanelNode> treeLists = new ArrayList<TreePanelNode>();
					String expressionofAlltrees = "";
					for (CoreMap sentence : sentences)
					{
						Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
						String eachSentenceofOneLine = tree.toString();
						expressionofAlltrees = expressionofAlltrees + eachSentenceofOneLine;
					}
					System.out.println(expressionofAlltrees);

					treeLists = new TreePanelNode().fromTextToTree(expressionofAlltrees);
					String sentencesofMultLines = "";
					int count = 0;
					for (TreePanelNode root : treeLists)
					{
						count++;
						if (root.examTheTree() && root != null)
							for (String word : root.changeIntoText())
								sentencesofMultLines = sentencesofMultLines + word;
						else
							sentencesofMultLines = sentencesofMultLines + "第" + count + "个括号表达式格式错误。";

						sentencesofMultLines = sentencesofMultLines + "\r\n";
					}

					editBracket.setText(sentencesofMultLines);

				}

				resetButtonstatus();

				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
			}
		});

		// 生成句法树按钮
		toTreeButtion.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println(new TreePanelNode().toOneLine(editBracket.getText()));
				System.out.println(new TreePanelNode().format(new TreePanelNode().toOneLine(editBracket.getText())));
				ArrayList<TreePanelNode> trees = new TreePanelNode().fromTextToTree(editBracket.getText());
				if (trees != null)
				{

					treeLists.clear();
					treeLists = trees;
					treeAtTxt.setTreeListWithOneTree(treeLists);
					nodes = TreePanelNode.nodesOfAllTrees(treeLists);
					for (TreePanelNode node : nodes)
					{
						node.calculateAngle();
					}
					// System.out.println(new TreePanelNode().toOneLine(editArea.getText()));
					// System.out.println(new TreePanelNode().format(new
					// TreePanelNode().toOneLine(editArea.getText())));
					hasModeified.put(treePanel.getTreeAtTxt().getTxtPath(), Boolean.FALSE);
					treePanel.setTreeAtTxt(treeAtTxt);
					treePanel.changeStatus_PanelModified();
					treePanel.setTreeLists(treeLists);
					treePanel.setNodes(nodes);
					resetButtonstatus();
					treePanel.setSelectedNodes(-1);
					treePanel.initCombineNodes();
					treePanel.repaint();
				}
				else
				{
					JOptionPane.showMessageDialog(frame, "括号表达式格式有错误,请检查。", "消息提示框", JOptionPane.INFORMATION_MESSAGE);
					resetButtonstatus();
					treePanel.setSelectedNodes(-1);
					treePanel.initCombineNodes();
					treePanel.repaint();
				}
			}
		});
		
		// 导出到文件按钮
		outputButton.addActionListener(new ActionListener()
		{// 将被修改为另存为

			public void actionPerformed(ActionEvent e)
			{
				treeLists = treePanel.getTreeLists();
				nodes = TreePanelNode.nodesOfAllTrees(treeLists);
				int returnValue = JOptionPane.YES_OPTION;
				for (TreePanelNode root : treeLists)
				{
					if (root.examTheTree() && root != null)
					{// 判断森林中的树是否都符合格式
						System.out.println(returnValue);
					}
					else
					{

						returnValue = JOptionPane.showConfirmDialog(frame, "括号表达式格式有错误,是否要保存。", "确认对话框",
								JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

						break;
					}

				}

				if (returnValue == JOptionPane.YES_OPTION && treeLists != null && nodes != null && !treeLists.isEmpty()
						&& !nodes.isEmpty())
				{
					try
					{
						int count = 0;
						SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
						System.out.println(date.format(new Date()) + ".txt");
						fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
						if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
						{// 确定保存
							String filePath = fileChooser.getSelectedFile().toString();
							FileOutputStream fos = new FileOutputStream(filePath);
							OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
							BufferedWriter bw = new BufferedWriter(osw);
							count = 0;
							for (TreePanelNode root : treeLists)
							{
								count++;
								if (root.examTheTree() && root != null)
									for (String word : root.changeIntoText())
										bw.write(word);
								else
									bw.write("第" + count + "个括号表达式格式错误。");

								bw.write("\r\n");
							}
							bw.close();

						}

					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();

			}
		});

		// 重画句法树按钮
		rePaintButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{

				nodes = treePanel.getNodes();
				treeLists = treePanel.getTreeLists();
				if (treeLists != null && nodes != null && !treeLists.isEmpty() && !nodes.isEmpty())
				{
					TreePanelNode.repaintTreeLists(treeLists);
					treePanel.setTreeLists(treeLists);
					treePanel.setNodes(TreePanelNode.nodesOfAllTrees(treeLists));

				}
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
			}
		});
		
		// 更新括号表达式按钮
		updateBracketButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				nodes = treePanel.getNodes();
				treeLists = treePanel.getTreeLists();
				int returnValue = JOptionPane.YES_OPTION;
				for (TreePanelNode root : treeLists)
				{
					if (root.examTheTree() && root != null)
					{// 判断森林中的树是否都符合格式

					}
					else
					{

						returnValue = JOptionPane.showConfirmDialog(frame, "括号表达式格式有错误,是否要更新括号表达式。", "确认对话框",
								JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

						break;
					}

				}
				if (returnValue == JOptionPane.YES_OPTION && treeLists != null && nodes != null && !treeLists.isEmpty()
						&& !nodes.isEmpty())
				{
					String allExpressionFormatted = "";
					int count = 0;
					for (TreePanelNode root : treeLists)
					{
						count++;
						if (root.examTheTree() && root != null)
							for (String word : root.changeIntoText())
								allExpressionFormatted = allExpressionFormatted + word;
						else
							allExpressionFormatted = allExpressionFormatted + "第" + count + "个括号表达式格式错误。";
						allExpressionFormatted = allExpressionFormatted + "\r\n";
					}
					editBracket.setText(allExpressionFormatted);
				}
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
			}
		});
		
		// 清空按钮
		clearPanel.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (!treeLists.isEmpty())
				{
					if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame, "是否清除画板？", "确认清除画板",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE))
					{
						treeLists.clear();
						nodes.clear();

						treePanel.setTreeLists(treeLists);
						treePanel.setNodes(nodes);
						treeAtTxt.setTreeListWithOneTree(treeLists);
						treePanel.setTreeAtTxt(treeAtTxt);
						hasModeified.put(treePanel.getTreeAtTxt().getTxtPath(), Boolean.TRUE);
						treePanel.setHasModeified(hasModeified);
//						resetButtonstatus();
//						treePanel.setSelectedNodes(-1);
//						treePanel.initCombineNodes();
//						treePanel.repaint();

					}
				}
				
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
			}
		});
		
		// 连接按钮
		combine.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				treePanel.setCombine_Clicked(!treePanel.isCombine_Clicked());
				treePanel.initCombineNodes();
				treePanel.setSelectedNodes(-1);// 不选中节点

				if (treePanel.isCombine_Clicked())
				{
					combine.setBackground(Color.green);
				}
				else
					combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setAdd_Clicked(false);
				add.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setDelete_Clicked(false);
				delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setSelectRoot_Clicked(false);
				selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));

				treePanel.grabFocus();
				treePanel.repaint();
			}
		});
		
		// 添加按钮
		add.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{

				treePanel.setSelectedNodes(-1);// 不选中节点
				
				treePanel.setAdd_Clicked(!treePanel.isAdd_Clicked());
				if (treePanel.isAdd_Clicked())
					add.setBackground(Color.green);
				else
					add.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setCombine_Clicked(false);
				combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setDelete_Clicked(false);
				delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setSelectRoot_Clicked(false);
				selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.grabFocus();
				treePanel.repaint();
			}
		});

		// 删除按钮
		delete.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				treePanel.setSelectedNodes(-1);// 不选中节点
				treePanel.setDelete_Clicked(!treePanel.isDelete_Clicked());
				
				if (treePanel.isDelete_Clicked())
					delete.setBackground(Color.green);
				else
					delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setCombine_Clicked(false);
				combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setAdd_Clicked(false);
				add.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.setSelectRoot_Clicked(false);
				selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				
				treePanel.grabFocus();
				treePanel.repaint();

			}
		});
		
		selectRoot.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				treePanel.setSelectedNodes(-1);// 不选中节点
				treePanel.setSelectRoot_Clicked(!treePanel.isSelectRoot_Clicked());
				if (treePanel.isSelectRoot_Clicked())
					selectRoot.setBackground(Color.green);
				else
					selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				treePanel.setCombine_Clicked(false);
				combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				// t.setModify_Clicked(false);
				// modify.setBackground((Color)new ColorUIResource(238,238,238));
				treePanel.setAdd_Clicked(false);
				add.setBackground((Color) new ColorUIResource(238, 238, 238));
				treePanel.setDelete_Clicked(false);
				delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				treePanel.grabFocus();
				treePanel.repaint();

			}
		});
		
		// 下一棵按钮
		rightTree.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				int returnValue = JOptionPane.YES_OPTION;
				String currentTxtPath = treePanel.getTreeAtTxt().getTxtPath();
				if (!hasModeified.get(currentTxtPath).booleanValue())
				{// txtPath对应文件没有修改
					// 向右滑动
					if (!nextTreeLists(TreeEditorTool.RIGHT)) // 向右滑动失败
						JOptionPane.showMessageDialog(null, "已经到最后一篇文档的最后一棵树。", "向右滑动",
								JOptionPane.INFORMATION_MESSAGE);
					else
					{
						rightTree.setBackground(Color.green);
						rightTree.setBackground((Color) new ColorUIResource(238, 238, 238));
					}
				}
				else
				{
					returnValue = JOptionPane.showConfirmDialog(frame, "必须先保存当前界面上的树。", "确认对话框",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{

						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (treePanel.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (treePanel.getTreeLists().get(0).examTheTree()
										&& treePanel.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();
											FileOutputStream fos = new FileOutputStream(currentTxtPath);
											OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
											BufferedWriter bw = new BufferedWriter(osw);
											for (String word : treePanel.getTreeLists().get(0).changeIntoText())
												bw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											treePanel.setHasModeified(hasModeified);
											treeAtTxt.setTxtPath(currentTxtPath);
											treePanel.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
											frame.setTitle(new File(currentTxtPath).getName() + "[ " + "1 / 1" + " ]");
											bw.close();

											// 向右滑动
											if (!nextTreeLists(TreeEditorTool.RIGHT)) // 向右滑动失败
												JOptionPane.showMessageDialog(null, "已经到最后一篇文档的最后一棵树。", "向右滑动",
														JOptionPane.INFORMATION_MESSAGE);

										}
										else
										{// 点击了取消或退出对话框，什么也不用做

										}

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}

								}
								else
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}

							}
							else
							{// 修改了打开的文件中的某棵树
								for (TreeAtTxt tat : allTreesAtTxt)
								{
									if (tat.getTxtPath().equals(currentTxtPath))
										treesOfSameTxt.add(tat.getTreeListWithOneTree().get(0));
								}
							}

							if (!treesOfSameTxt.isEmpty())
							{// 执行了上面的else
								boolean correctFormat = true;
								for (TreePanelNode treeRoot : treesOfSameTxt)
								{
									if (treeRoot.examTheTree() && treeRoot != null)
									{

									}
									else
									{
										correctFormat = false;
										break;
									}
								}
								if (correctFormat == false)
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileOutputStream fos = new FileOutputStream(currentTxtPath);
										OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
										BufferedWriter bw = new BufferedWriter(osw);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												bw.write(word);
											bw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										treePanel.setHasModeified(hasModeified);
										bw.close();
										// 向右滑动
										if (!nextTreeLists(TreeEditorTool.RIGHT)) // 向右滑动失败
											JOptionPane.showMessageDialog(null, "已经到最后一篇文档的最后一棵树。", "向右滑动",
													JOptionPane.INFORMATION_MESSAGE);

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}
								}

							}

						}
						else
						{// 当前面板不止一棵树/或者没有树
							JOptionPane.showMessageDialog(frame, "画板上只有一棵树时才能保存。", "消息提示框",
									JOptionPane.INFORMATION_MESSAGE);
						}

					}
					else if (returnValue == JOptionPane.NO_OPTION)
					{
						hasModeified.put(currentTxtPath, Boolean.FALSE);
						treePanel.setHasModeified(hasModeified);
						// 向右滑动
						if (!nextTreeLists(TreeEditorTool.RIGHT)) // 向右滑动失败
							JOptionPane.showMessageDialog(null, "已经到最后一篇文档的最后一棵树。", "向右滑动",
									JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{// 点击了取消或者退出
					}
				}
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
			}
		});
		
		// 上一棵按钮
		leftTree.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				int returnValue = JOptionPane.YES_OPTION;
				String currentTxtPath = treePanel.getTreeAtTxt().getTxtPath();
				if (!hasModeified.get(currentTxtPath).booleanValue())
				{// txtPath对应文件没有修改
					// 向左滑动
					if (!nextTreeLists(TreeEditorTool.LEFT)) // 向左滑动失败
						JOptionPane.showMessageDialog(null, "已经到第一篇文档的第一棵树。", "向左滑动", JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					returnValue = JOptionPane.showConfirmDialog(frame, "请先保存当前页面的内容。", "确认对话框",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{
						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (treePanel.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (treePanel.getTreeLists().get(0).examTheTree()
										&& treePanel.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();
											FileOutputStream fos = new FileOutputStream(currentTxtPath);
											OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
											BufferedWriter bw = new BufferedWriter(osw);
											for (String word : treePanel.getTreeLists().get(0).changeIntoText())
												bw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											treePanel.setHasModeified(hasModeified);
											treeAtTxt.setTxtPath(currentTxtPath);
											treePanel.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
											frame.setTitle(new File(currentTxtPath).getName() + "[ " + "1 / 1" + " ]");
											bw.close();

											// 向左滑动
											if (!nextTreeLists(TreeEditorTool.LEFT)) // 向左滑动失败
												JOptionPane.showMessageDialog(null, "已经到第一篇文档的第一棵树。", "向左滑动",
														JOptionPane.INFORMATION_MESSAGE);

										}
										else
										{// 点击了取消或退出对话框，什么也不用做

										}

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}

								}
								else
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}

							}
							else
							{// 修改了打开的文件中的某棵树
								for (TreeAtTxt tat : allTreesAtTxt)
								{
									if (tat.getTxtPath().equals(currentTxtPath))
										treesOfSameTxt.add(tat.getTreeListWithOneTree().get(0));
								}
							}

							if (!treesOfSameTxt.isEmpty())
							{// 执行了上面的else
								boolean correctFormat = true;
								for (TreePanelNode treeRoot : treesOfSameTxt)
								{
									if (treeRoot.examTheTree() && treeRoot != null)
									{

									}
									else
									{
										correctFormat = false;
										break;
									}
								}
								if (correctFormat == false)
								{
									JOptionPane.showMessageDialog(frame, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileOutputStream fos = new FileOutputStream(currentTxtPath);
										OutputStreamWriter osw = new OutputStreamWriter(fos, charsetName);
										BufferedWriter bw = new BufferedWriter(osw);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												bw.write(word);
											bw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										treePanel.setHasModeified(hasModeified);
										bw.close();
										// 向左滑动
										if (!nextTreeLists(TreeEditorTool.LEFT)) // 向左滑动失败
											JOptionPane.showMessageDialog(null, "已经到第一篇文档的第一棵树。", "向左滑动",
													JOptionPane.INFORMATION_MESSAGE);

									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}
								}

							}

						}
						else
						{// 当前面板不止一棵树/或者没有树
							JOptionPane.showMessageDialog(frame, "画板上只有一棵树时才能保存。", "消息提示框",
									JOptionPane.INFORMATION_MESSAGE);
						}

					}
					else if (returnValue == JOptionPane.NO_OPTION)
					{
						// 向左滑动
						hasModeified.put(currentTxtPath, Boolean.FALSE);
						treePanel.setHasModeified(hasModeified);
						if (!nextTreeLists(TreeEditorTool.LEFT)) // 向左滑动失败
							JOptionPane.showMessageDialog(null, "已经到第一篇文档的第一棵树。", "向左滑动",
									JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{// 点击了取消或者退出
					}
				}
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
			}
		});
	}

	private void resetButtonstatus()
	{
		treePanel.setDelete_Clicked(false);
		treePanel.getDelete().setBackground((Color) new ColorUIResource(238, 238, 238));

		treePanel.setAdd_Clicked(false);
		treePanel.getAdd().setBackground((Color) new ColorUIResource(238, 238, 238));

		treePanel.setCombine_Clicked(false);
		treePanel.getCombine().setBackground((Color) new ColorUIResource(238, 238, 238));

		treePanel.setSelectRoot_Clicked(false);
		treePanel.getSelectRoot().setBackground((Color) new ColorUIResource(238, 238, 238));
	}

	private void resetTreePanel()
	{
		treeLists.clear();
		allTreesAtTxt.clear();
		nodes.clear();
		hasModeified.clear();
		hasModeified.put(null, Boolean.FALSE);
		treeAtTxt = new TreeAtTxt(treeLists);
		allTreesAtTxt.add(treeAtTxt);
		treePanel.setTreeLists(treeLists);
		treePanel.setNodes(nodes);
		treePanel.setHasModeified(hasModeified);
		treePanel.setTreeAtTxt(treeAtTxt);
		resetButtonstatus();
		treePanel.setSelectedNodes(-1);
		treePanel.initCombineNodes();
		treePanel.repaint();
		frame.setTitle("未命名.txt" + "[ " + 1 + " / " + 1 + " ]");
		System.out.println("面板被清空了");
	}

	private boolean nextTreeLists(int direction)
	{
		int sizeOfTrees = allTreesAtTxt.size();
		int positionOfTreeAtList = allTreesAtTxt.indexOf(treePanel.getTreeAtTxt());
		System.out.println("positionOfTreeAtList" + positionOfTreeAtList);
		if (direction == TreeEditorTool.LEFT)
		{// 向左滑动
			if (positionOfTreeAtList != 0)
			{
				treeLists = allTreesAtTxt.get(positionOfTreeAtList - 1).getTreeListWithOneTree();
				nodes = TreePanelNode.nodesOfAllTrees(treeLists);
				treeAtTxt = allTreesAtTxt.get(positionOfTreeAtList - 1);
				treePanel.setTreeLists(treeLists);
				treePanel.setNodes(nodes);
				treePanel.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.setTreeLists(treeLists);
				treePanel.setNodes(nodes);
				treePanel.setHasModeified(hasModeified);
				treePanel.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
				frame.setTitle(new File(treePanel.getTreeAtTxt().getTxtPath()).getName() + "[ "
						+ treeAtTxt.treePositionAtTxt(allTreesAtTxt) + " ]");
				return true;
			}
			else
				return false;
		}
		else
		{// 向右滑动
			if (positionOfTreeAtList != sizeOfTrees - 1)
			{
				treeLists = allTreesAtTxt.get(positionOfTreeAtList + 1).getTreeListWithOneTree();
				nodes = TreePanelNode.nodesOfAllTrees(treeLists);
				treeAtTxt = allTreesAtTxt.get(positionOfTreeAtList + 1);
				treePanel.setTreeLists(treeLists);
				treePanel.setNodes(nodes);
				treePanel.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.setTreeLists(treeLists);
				treePanel.setNodes(nodes);
				treePanel.setHasModeified(hasModeified);
				treePanel.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				treePanel.setSelectedNodes(-1);
				treePanel.initCombineNodes();
				treePanel.repaint();
				frame.setTitle(new File(treePanel.getTreeAtTxt().getTxtPath()).getName() + "[ "
						+ treeAtTxt.treePositionAtTxt(allTreesAtTxt) + " ]");
				return true;
			}
			else
				return false;
		}

	}

	private void openTxts() throws IOException
	{
		fileChooser.addChoosableFileFilter(txtFileFilter);
		fileChooser.setMultiSelectionEnabled(true);
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File[] files = fileChooser.getSelectedFiles();
			for (int i = 0, j = files.length - 1; i < files.length; i++, j--)
			{// 由于JFileChooser会将先选择的文件后打开，故将数组files翻转
				if (i < j)
				{
					File temp = files[i];
					files[i] = files[j];
					files[j] = temp;
				}
				else
					break;
			}

			hasModeified.clear();
			allTreesAtTxt.clear();

			for (File file : files)
			{
				FileInputStream fis = new FileInputStream(file.toString());
				InputStreamReader isr = new InputStreamReader(fis, charsetName);
				BufferedReader br = new BufferedReader(isr);

				String strOfaTxt = new String();
				String line = null;
				while ((line = br.readLine()) != null)
				{
					strOfaTxt += line;
				}
				br.close();

				System.out.println(strOfaTxt);

				ArrayList<TreePanelNode> trees = new TreePanelNode().fromTextToTree(strOfaTxt);
				hasModeified.put(file.toString(), Boolean.FALSE);

				for (TreePanelNode tree : trees)
				{
					TreePanelNode.allocatePosition(tree);
					TreeAtTxt treeAtTxt = new TreeAtTxt(tree, file.toString());
					allTreesAtTxt.add(treeAtTxt);
				}
			}

			treeLists = allTreesAtTxt.get(0).getTreeListWithOneTree();
			treePanel.setTreeLists(treeLists);
			nodes = TreePanelNode.nodesOfAllTrees(treeLists);
			treePanel.setNodes(nodes);
			treeAtTxt = allTreesAtTxt.get(0);

			treePanel.setTreeAtTxt(treeAtTxt);
			treePanel.setHasModeified(hasModeified);

			resetButtonstatus();

			treePanel.setSelectedNodes(-1);
			treePanel.initCombineNodes();
			treePanel.repaint();

			frame.setTitle(files[0].getName() + "[ " + treeAtTxt.treePositionAtTxt(allTreesAtTxt) + " ]");

		}
	}

	private void coreNLPInit()
	{
		Properties props = new Properties();
		try
		{
			props.load(this.getClass().getClassLoader().getResourceAsStream("StanfordCoreNLP-chinese.properties"));
			props.setProperty("annotators", "tokenize,ssplit,pos,parse");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		pipeline = new StanfordCoreNLP(props);
		// annotation = new Annotation("句法分析。");
		// pipeline.annotate(annotation);
	}

	public static void main(String args[])
	{
		new TreeEditorTool().init();
	}

}