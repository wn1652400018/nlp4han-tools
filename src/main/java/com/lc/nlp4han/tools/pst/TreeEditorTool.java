package com.lc.nlp4han.tools.pst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
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

import javax.swing.plaf.ColorUIResource;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class TreeEditorTool
{
	private JFrame f = new JFrame("未命名.txt" + "[ " + 1 + " / " + 1 + " ]");
	private TreePanel t = new TreePanel();
	private JScrollPane paintjsp;
	private JPanel editPanel = new JPanel();
	private TextArea editArea0 = new TextArea(2, 80);
	private TextArea editArea = new TextArea(8, 80);
	private JButton start = new JButton("生成结构树");
	private JButton treeToText = new JButton("导出到文件");
	private JButton rePaint = new JButton("重画结构树");
	private JButton updateExpression = new JButton("更新表达式");
	private JButton parse = new JButton("句法分析");
	private JButton importExpressionFromText = new JButton("导入表达式");
	private JFileChooser fileChooser = new JFileChooser();
	private TxtFileFilter txtFileFilter = new TxtFileFilter();
	private Vector<TreePanelNode> nodes = new Vector<TreePanelNode>();
	private ArrayList<TreePanelNode> treeLists = new ArrayList<TreePanelNode>();
	private HashMap<String, Boolean> hasModeified = new HashMap<String, Boolean>(); // 记录文本是否被改变
	private TreeAtTxt treeAtTxt = new TreeAtTxt();// 表示当前面板上的括号表达式
	private ArrayList<TreeAtTxt> allTreesAtTxt = new ArrayList<TreeAtTxt>();// 表示所有文件中的括号表达式
	private static int RIGHT = 0, LEFT = -1;// 表示左右滑动
	private StanfordCoreNLP pipeline;
	private Annotation annotation;

	public void init()
	{

		// coreNLPInit();
		treeAtTxt = new TreeAtTxt(treeLists);
		allTreesAtTxt.add(treeAtTxt);
		hasModeified.put(null, Boolean.FALSE);
		t.setHasModeified(hasModeified);
		t.setTreeAtTxt(treeAtTxt);
		t.setNodes(nodes);
		t.setTreeLists(treeLists);

		t.setPreferredSize(new Dimension(10000, 10000));
		paintjsp = new JScrollPane(t);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(parse);
		buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(start);
		buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.add(rePaint);
		buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.add(updateExpression);
		buttonPanel.add(Box.createVerticalStrut(8));
		// buttonPanel.add(importExpressionFromText);
		// buttonPanel.add(Box.createVerticalStrut(8));
		buttonPanel.add(treeToText);

		// 创建菜单
		JMenuBar jmb = new JMenuBar();
		JMenu jm = new JMenu("文件");
		JMenuItem jmi_new = new JMenuItem("新建");
		JMenuItem jmi_open = new JMenuItem("打开");
		JMenuItem jmi_save = new JMenuItem("保存");
		JMenuItem jmi_saveAs = new JMenuItem("另存为");
		jm.add(jmi_new);
		jm.add(jmi_open);
		jm.add(jmi_save);
		jm.add(jmi_saveAs);
		jmb.add(jm);

		JScrollPane editjsp0 = new JScrollPane(editArea0); // 输入未处理的句子
		JScrollPane editjsp = new JScrollPane(editArea);// 输入括号表达式
		JPanel editTextPanel = new JPanel();
		editTextPanel.setLayout(new BorderLayout());
		editTextPanel.add(editjsp);
		editTextPanel.add(editjsp0, BorderLayout.NORTH);

		editPanel.setLayout(new BorderLayout());
		editPanel.add(jmb, BorderLayout.NORTH);
		editPanel.add(editTextPanel);
		editPanel.add(buttonPanel, BorderLayout.EAST);

		JPanel functionPanel = new JPanel();
		functionPanel.setLayout(new BoxLayout(functionPanel, BoxLayout.Y_AXIS));
		JButton add = new JButton("增加");
		JButton delete = new JButton("删除");
		// JButton modify = new JButton("修改");
		JButton combine = new JButton("连接");
		JButton selectRoot = new JButton("root");
		JButton clearPanel = new JButton("清空");
		JButton leftTree = new JButton("<——");
		JButton rightTree = new JButton("——>");

		functionPanel.add(add);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(delete);
		functionPanel.add(Box.createVerticalStrut(8));
		// functionPanel.add(modify);
		// functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(combine);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(selectRoot);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(clearPanel);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(leftTree);
		functionPanel.add(Box.createVerticalStrut(8));
		functionPanel.add(rightTree);

		t.setAdd(add);
		t.setDelete(delete);
		// t.setModify(modify);
		t.setCombine(combine);
		t.setSelectRoot(selectRoot);
		System.out.println(222222);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(paintjsp);
		panel.add(functionPanel, BorderLayout.WEST);
		panel.add(editPanel, BorderLayout.NORTH);

		double width = Toolkit.getDefaultToolkit().getScreenSize().width; // 得到当前屏幕分辨率的高
		double height = Toolkit.getDefaultToolkit().getScreenSize().height - 45;// 得到当前屏幕分辨率的宽
		f.setSize((int) width, (int) height);// 设置大小
		f.setLocation(0, 0); // 设置窗体居中显示
		// f.setResizable(false);// 禁用最大化按钮
		f.setLocationRelativeTo(null);
		f.add(panel);

		f.setJMenuBar(jmb);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);

		// 测试
		// 为文件菜单添加点击事件
		jmi_new.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int returnValue = JOptionPane.YES_OPTION;
				String currentTxtPath = t.getTreeAtTxt().getTxtPath();
				if (!hasModeified.get(currentTxtPath).booleanValue())
				{// txtPath对应文件没有修改
					// 新建
					resetTreePanel();
				}
				else
				{
					returnValue = JOptionPane.showConfirmDialog(f, "是否要保存,当前页面的内容。", "确认对话框",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{
						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (t.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (t.getTreeLists().get(0).examTheTree() && t.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();
											FileWriter fw = new FileWriter(currentTxtPath);
											for (String word : t.getTreeLists().get(0).changeIntoText())
												fw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											t.setHasModeified(hasModeified);
											t.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null

											fw.close();

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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileWriter fw = new FileWriter(currentTxtPath);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												fw.write(word);
											fw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										t.setHasModeified(hasModeified);
										fw.close();
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
							JOptionPane.showMessageDialog(f, "画板上只有一棵树时才能保存。", "消息提示框",
									JOptionPane.INFORMATION_MESSAGE);
						}

					}
					else if (returnValue == JOptionPane.NO_OPTION)
					{
						// 新建
						resetTreePanel();
					}
					else
					{// 点击了取消或者退出
						resetButtonstatus();
						t.initCombineNodes();
						t.setSelectedNodes(-1);
						t.repaint();
					}
				}
				resetButtonstatus();
				t.initCombineNodes();
				t.setSelectedNodes(-1);
				t.repaint();
			}
		});

		jmi_open.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int returnValue = JOptionPane.YES_OPTION;
				String currentTxtPath = t.getTreeAtTxt().getTxtPath();
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
					returnValue = JOptionPane.showConfirmDialog(f, "是否要保存,当前页面的内容。", "确认对话框",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{

						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (t.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (t.getTreeLists().get(0).examTheTree() && t.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();
											FileWriter fw = new FileWriter(currentTxtPath);
											for (String word : t.getTreeLists().get(0).changeIntoText())
												fw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											t.setHasModeified(hasModeified);
											t.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
											f.setTitle(new File(currentTxtPath).getName() + "[ " + "1 / 1" + " ]");
											fw.close();

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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileWriter fw = new FileWriter(currentTxtPath);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												fw.write(word);
											fw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										t.setHasModeified(hasModeified);
										fw.close();
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
							JOptionPane.showMessageDialog(f, "画板上只有一棵树时才能保存。", "消息提示框",
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
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
			}
		});

		jmi_save.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (t.getHasModeified().get(t.getTreeAtTxt().getTxtPath()))
				{// 文件被修改过
					if (t.getTreeLists().size() != 1)
					{
						JOptionPane.showMessageDialog(f, "保存时画板必须只有一棵树", "消息提示框", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{// 要保存的面板中只有一棵树
						if (t.getTreeLists().get(0).examTheTree() && t.getTreeLists().get(0) != null)
						{// 格式正确
							if (t.getTreeAtTxt().getTxtPath() == null)
							{// 还没有为当前文件设置路径和名称
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
								fileChooser.setSelectedFile(new File(sdf.format(new Date()) + ".txt"));
								if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
								{
									String currentTxtPath = fileChooser.getSelectedFile().toString();
									try
									{
										FileWriter fw = new FileWriter(currentTxtPath);
										for (String word : t.getTreeLists().get(0).changeIntoText())
											fw.write(word);

										hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										t.setHasModeified(hasModeified);
										t.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
										resetButtonstatus();
										t.setSelectedNodes(-1);
										t.initCombineNodes();
										f.setTitle(fileChooser.getSelectedFile().getName() + "[ " + "1 / 1" + " ]");
										t.repaint();
										fw.close();
									}
									catch (IOException e1)
									{
										e1.printStackTrace();
									}
								}
							}
							else
							{// 该文件是打开已有的文本
								String currentTxtPath = t.getTreeAtTxt().getTxtPath();
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
										JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
												JOptionPane.INFORMATION_MESSAGE);
									}
									else
									{
										try
										{
											FileWriter fw = new FileWriter(currentTxtPath);
											for (TreePanelNode treeRoot : treesOfSameTxt)
											{
												for (String word : treeRoot.changeIntoText())
													fw.write(word);
												fw.write("\r\n");
											}
											// 修改面板的部分状态
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											t.setHasModeified(hasModeified);
											resetButtonstatus();
											t.setSelectedNodes(-1);
											t.initCombineNodes();
											t.repaint();
											fw.close();

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
							JOptionPane.showMessageDialog(f, "树的格式不正确。", "树的格式不正确", JOptionPane.INFORMATION_MESSAGE);
							resetButtonstatus();
							t.setSelectedNodes(-1);
							t.initCombineNodes();
							t.repaint();
						}
					}
				}
				else
				{// 文件没有被修改
					// 暂时认为不用管
					resetButtonstatus();
					t.setSelectedNodes(-1);
					t.initCombineNodes();
					t.repaint();
				}
			}

		});

		jmi_saveAs.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				treeLists = t.getTreeLists();
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

						returnValue = JOptionPane.showConfirmDialog(f, "括号表达式格式有错误,是否要保存。", "确认对话框",
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
							FileWriter treeToText = new FileWriter(filePath);
							count = 0;
							for (TreePanelNode root : treeLists)
							{
								count++;
								if (root.examTheTree() && root != null)
									for (String word : root.changeIntoText())
										treeToText.write(word);
								else
									treeToText.write("第" + count + "个括号表达式格式错误。");

								treeToText.write("\r\n");
							}
							treeToText.close();

						}
						resetButtonstatus();
						t.initCombineNodes();
						t.setSelectedNodes(-1);
						t.repaint();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});

		parse.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				String text = editArea0.getText();
				if (text.trim().length() != 0)
				{// 将无格式的括号表达式转化为有格式的表达式
					annotation = new Annotation(text);
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
					editArea.setText(sentencesofMultLines);

				}
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
			}
		});

		start.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.out.println(new TreePanelNode().toOneLine(editArea.getText()));
				System.out.println(new TreePanelNode().format(new TreePanelNode().toOneLine(editArea.getText())));
				ArrayList<TreePanelNode> trees = new TreePanelNode().fromTextToTree(editArea.getText());
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
					hasModeified.put(t.getTreeAtTxt().getTxtPath(), Boolean.FALSE);
					t.setTreeAtTxt(treeAtTxt);
					t.changeStatus_PanelModified();
					t.setTreeLists(treeLists);
					t.setNodes(nodes);
					resetButtonstatus();
					t.setSelectedNodes(-1);
					t.initCombineNodes();
					t.repaint();
				}
				else
				{
					JOptionPane.showMessageDialog(f, "括号表达式格式有错误,请检查。", "消息提示框", JOptionPane.INFORMATION_MESSAGE);
					resetButtonstatus();
					t.setSelectedNodes(-1);
					t.initCombineNodes();
					t.repaint();
				}
			}
		});
		treeToText.addActionListener(new ActionListener()
		{// 将被修改为另存为

			@Override
			public void actionPerformed(ActionEvent e)
			{
				treeLists = t.getTreeLists();
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

						returnValue = JOptionPane.showConfirmDialog(f, "括号表达式格式有错误,是否要保存。", "确认对话框",
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
							FileWriter treeToText = new FileWriter(filePath);
							count = 0;
							for (TreePanelNode root : treeLists)
							{
								count++;
								if (root.examTheTree() && root != null)
									for (String word : root.changeIntoText())
										treeToText.write(word);
								else
									treeToText.write("第" + count + "个括号表达式格式错误。");

								treeToText.write("\r\n");
							}
							treeToText.close();

						}

					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();

			}
		});

		rePaint.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{

				nodes = t.getNodes();
				treeLists = t.getTreeLists();
				if (treeLists != null && nodes != null && !treeLists.isEmpty() && !nodes.isEmpty())
				{
					TreePanelNode.repaintTreeLists(treeLists);
					t.setTreeLists(treeLists);
					t.setNodes(TreePanelNode.nodesOfAllTrees(treeLists));

				}
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
			}
		});
		updateExpression.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				nodes = t.getNodes();
				treeLists = t.getTreeLists();
				int returnValue = JOptionPane.YES_OPTION;
				for (TreePanelNode root : treeLists)
				{
					if (root.examTheTree() && root != null)
					{// 判断森林中的树是否都符合格式

					}
					else
					{

						returnValue = JOptionPane.showConfirmDialog(f, "括号表达式格式有错误,是否要更新括号表达式。", "确认对话框",
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
					editArea.setText(allExpressionFormatted);
				}
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
			}
		});
		clearPanel.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!treeLists.isEmpty())
				{
					if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(f, "是否清除画板？", "确认清除画板",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE))
					{
						treeLists.clear();
						nodes.clear();

						t.setTreeLists(treeLists);
						t.setNodes(nodes);
						treeAtTxt.setTreeListWithOneTree(treeLists);
						t.setTreeAtTxt(treeAtTxt);
						hasModeified.put(t.getTreeAtTxt().getTxtPath(), Boolean.TRUE);
						t.setHasModeified(hasModeified);
						resetButtonstatus();
						t.setSelectedNodes(-1);
						t.initCombineNodes();
						t.repaint();

					}
				}
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
			}
		});
		combine.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				t.setCombine_Clicked(!t.isCombine_Clicked());
				t.initCombineNodes();
				t.setSelectedNodes(-1);// 不选中节点

				if (t.isCombine_Clicked())
				{
					combine.setBackground(Color.green);
				}
				else
					combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setAdd_Clicked(false);
				add.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setDelete_Clicked(false);
				delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setSelectRoot_Clicked(false);
				selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				// t.setModify_Clicked(false);
				// modify.setBackground((Color)new ColorUIResource(238,238,238));
				t.grabFocus();
				t.repaint();
			}
		});
		add.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{

				t.setSelectedNodes(-1);// 不选中节点
				t.setAdd_Clicked(!t.isAdd_Clicked());
				if (t.isAdd_Clicked())
					add.setBackground(Color.green);
				else
					add.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setCombine_Clicked(false);
				combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setDelete_Clicked(false);
				delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setSelectRoot_Clicked(false);
				selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				// t.setModify_Clicked(false);
				// modify.setBackground((Color)new ColorUIResource(238,238,238));
				t.grabFocus();
				t.repaint();
			}
		});

		delete.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				t.setSelectedNodes(-1);// 不选中节点
				t.setDelete_Clicked(!t.isDelete_Clicked());
				if (t.isDelete_Clicked())
					delete.setBackground(Color.green);
				else
					delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setCombine_Clicked(false);
				combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				// t.setModify_Clicked(false);
				// modify.setBackground((Color)new ColorUIResource(238,238,238));
				t.setAdd_Clicked(false);
				add.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setSelectRoot_Clicked(false);
				selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.grabFocus();
				t.repaint();

			}
		});
		selectRoot.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				t.setSelectedNodes(-1);// 不选中节点
				t.setSelectRoot_Clicked(!t.isSelectRoot_Clicked());
				if (t.isSelectRoot_Clicked())
					selectRoot.setBackground(Color.green);
				else
					selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setCombine_Clicked(false);
				combine.setBackground((Color) new ColorUIResource(238, 238, 238));
				// t.setModify_Clicked(false);
				// modify.setBackground((Color)new ColorUIResource(238,238,238));
				t.setAdd_Clicked(false);
				add.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.setDelete_Clicked(false);
				delete.setBackground((Color) new ColorUIResource(238, 238, 238));
				t.grabFocus();
				t.repaint();

			}
		});
		rightTree.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int returnValue = JOptionPane.YES_OPTION;
				String currentTxtPath = t.getTreeAtTxt().getTxtPath();
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
					returnValue = JOptionPane.showConfirmDialog(f, "必须先保存当前界面上的树。", "确认对话框",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{

						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (t.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (t.getTreeLists().get(0).examTheTree() && t.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();
											FileWriter fw = new FileWriter(currentTxtPath);
											for (String word : t.getTreeLists().get(0).changeIntoText())
												fw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											t.setHasModeified(hasModeified);
											treeAtTxt.setTxtPath(currentTxtPath);
											t.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
											f.setTitle(new File(currentTxtPath).getName() + "[ " + "1 / 1" + " ]");
											fw.close();

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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileWriter fw = new FileWriter(currentTxtPath);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												fw.write(word);
											fw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										t.setHasModeified(hasModeified);
										fw.close();
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
							JOptionPane.showMessageDialog(f, "画板上只有一棵树时才能保存。", "消息提示框",
									JOptionPane.INFORMATION_MESSAGE);
						}

					}
					else if (returnValue == JOptionPane.NO_OPTION)
					{
						hasModeified.put(currentTxtPath, Boolean.FALSE);
						t.setHasModeified(hasModeified);
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
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
			}
		});
		leftTree.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int returnValue = JOptionPane.YES_OPTION;
				String currentTxtPath = t.getTreeAtTxt().getTxtPath();
				if (!hasModeified.get(currentTxtPath).booleanValue())
				{// txtPath对应文件没有修改
					// 向左滑动
					if (!nextTreeLists(TreeEditorTool.LEFT)) // 向左滑动失败
						JOptionPane.showMessageDialog(null, "已经到第一篇文档的第一棵树。", "向左滑动", JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					returnValue = JOptionPane.showConfirmDialog(f, "请先保存当前页面的内容。", "确认对话框",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (returnValue == JOptionPane.YES_OPTION)
					{
						ArrayList<TreePanelNode> treesOfSameTxt = new ArrayList<TreePanelNode>();
						if (t.getTreeLists().size() == 1)
						{// 当前面板上只有一棵树是才可以保存
							if (currentTxtPath == null)
							{// 新建的面板，代表一个txt中只有一棵树
								if (t.getTreeLists().get(0).examTheTree() && t.getTreeLists().get(0) != null)
								{
									try
									{
										SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
										fileChooser.setSelectedFile(new File(date.format(new Date()) + ".txt"));
										if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
										{// 确定保存
											currentTxtPath = fileChooser.getSelectedFile().toString();
											FileWriter fw = new FileWriter(currentTxtPath);
											for (String word : t.getTreeLists().get(0).changeIntoText())
												fw.write(word);

											hasModeified.remove(null);// 将没有设置位置和文件名的删除，因为已经为它保存到了指定文件中
											hasModeified.put(currentTxtPath, Boolean.FALSE);
											t.setHasModeified(hasModeified);
											treeAtTxt.setTxtPath(currentTxtPath);
											t.getTreeAtTxt().setTxtPath(currentTxtPath);// 修改文件的路径，之前是null
											f.setTitle(new File(currentTxtPath).getName() + "[ " + "1 / 1" + " ]");
											fw.close();

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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
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
									JOptionPane.showMessageDialog(f, "结构树格式有错误,请检查。", "消息提示框",
											JOptionPane.INFORMATION_MESSAGE);
								}
								else
								{
									try
									{
										FileWriter fw = new FileWriter(currentTxtPath);
										for (TreePanelNode treeRoot : treesOfSameTxt)
										{
											for (String word : treeRoot.changeIntoText())
												fw.write(word);
											fw.write("\r\n");
										}
										hasModeified.put(currentTxtPath, Boolean.FALSE);
										t.setHasModeified(hasModeified);
										fw.close();
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
							JOptionPane.showMessageDialog(f, "画板上只有一棵树时才能保存。", "消息提示框",
									JOptionPane.INFORMATION_MESSAGE);
						}

					}
					else if (returnValue == JOptionPane.NO_OPTION)
					{
						// 向左滑动
						hasModeified.put(currentTxtPath, Boolean.FALSE);
						t.setHasModeified(hasModeified);
						if (!nextTreeLists(TreeEditorTool.LEFT)) // 向左滑动失败
							JOptionPane.showMessageDialog(null, "已经到第一篇文档的第一棵树。", "向左滑动",
									JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{// 点击了取消或者退出
					}
				}
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
			}
		});
	}

	private void resetButtonstatus()
	{
		t.setDelete_Clicked(false);
		t.getDelete().setBackground((Color) new ColorUIResource(238, 238, 238));
		t.setAdd_Clicked(false);
		t.getAdd().setBackground((Color) new ColorUIResource(238, 238, 238));
		t.setCombine_Clicked(false);
		t.getCombine().setBackground((Color) new ColorUIResource(238, 238, 238));
		t.setSelectRoot_Clicked(false);
		t.getSelectRoot().setBackground((Color) new ColorUIResource(238, 238, 238));
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
		t.setTreeLists(treeLists);
		t.setNodes(nodes);
		t.setHasModeified(hasModeified);
		t.setTreeAtTxt(treeAtTxt);
		resetButtonstatus();
		t.setSelectedNodes(-1);
		t.initCombineNodes();
		t.repaint();
		f.setTitle("未命名.txt" + "[ " + 1 + " / " + 1 + " ]");
		System.out.println("面板被清空了");
	}

	private boolean nextTreeLists(int direction)
	{
		int sizeOfTrees = allTreesAtTxt.size();
		int positionOfTreeAtList = allTreesAtTxt.indexOf(t.getTreeAtTxt());
		System.out.println("positionOfTreeAtList" + positionOfTreeAtList);
		if (direction == TreeEditorTool.LEFT)
		{// 向左滑动
			if (positionOfTreeAtList != 0)
			{
				treeLists = allTreesAtTxt.get(positionOfTreeAtList - 1).getTreeListWithOneTree();
				nodes = TreePanelNode.nodesOfAllTrees(treeLists);
				treeAtTxt = allTreesAtTxt.get(positionOfTreeAtList - 1);
				t.setTreeLists(treeLists);
				t.setNodes(nodes);
				t.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.setTreeLists(treeLists);
				t.setNodes(nodes);
				t.setHasModeified(hasModeified);
				t.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
				f.setTitle(new File(t.getTreeAtTxt().getTxtPath()).getName() + "[ "
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
				t.setTreeLists(treeLists);
				t.setNodes(nodes);
				t.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.setTreeLists(treeLists);
				t.setNodes(nodes);
				t.setHasModeified(hasModeified);
				t.setTreeAtTxt(treeAtTxt);
				resetButtonstatus();
				t.setSelectedNodes(-1);
				t.initCombineNodes();
				t.repaint();
				f.setTitle(new File(t.getTreeAtTxt().getTxtPath()).getName() + "[ "
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
				BufferedReader br = new BufferedReader(new FileReader(file.toString()));
				String strOfaTxt = new String();
				String line = null;
				while ((line = br.readLine()) != null)
				{
					strOfaTxt += line;
				}
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
			t.setTreeLists(treeLists);
			nodes = TreePanelNode.nodesOfAllTrees(treeLists);
			t.setNodes(nodes);
			treeAtTxt = allTreesAtTxt.get(0);
			t.setTreeAtTxt(treeAtTxt);
			t.setHasModeified(hasModeified);
			resetButtonstatus();
			t.setSelectedNodes(-1);
			t.initCombineNodes();
			t.repaint();
			f.setTitle(files[0].getName() + "[ " + treeAtTxt.treePositionAtTxt(allTreesAtTxt) + " ]");

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
		annotation = new Annotation("句法分析。");
		pipeline.annotate(annotation);
	}

	public static void main(String args[])
	{
		new TreeEditorTool().init();
	}

}
