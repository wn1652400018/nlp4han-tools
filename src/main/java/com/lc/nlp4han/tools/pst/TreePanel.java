package com.lc.nlp4han.tools.pst;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.ColorUIResource;

public class TreePanel extends JPanel
{
	private Vector<TreePanelNode> nodes;
	private ArrayList<TreePanelNode> treeLists;
	private TreeAtTxt treeAtTxt;
	private HashMap<String, Boolean> hasModeified;// 记录文本是否被改变

	private int selectedNodes = -1;// 默认表示没有选中节点
	private ArrayList<Integer> selectedALines = new ArrayList<Integer>();
	private int selectedALine = -1;// 默认没有选中线。selectedALine表示 被选中线的箭头指向的节点 与其父节点之间的连线
	private int tempX;// 记录鼠标点击时的坐标，或者记录拖动时鼠标的上一个坐标
	private int tempY;// 记录鼠标点击时的坐标，或者记录拖动时鼠标的上一个坐标
	private int mouseX; // 表示任意时刻鼠标的位置
	private int mouseY;// 表示任意时刻鼠标的位置
	private int editingNode = -1;// 表示被编辑的节点的序号，默认是没有要编辑的节点
	private int parentOfAddingNode = -1;// 表示要添加节点的父亲节点，默认为-1即不添加节点
	private int[] combinedNodes = { -1, -1 };// 表示将要链接的两个节点
	private int count = -1;// 用来表示添加到数组combinedNodes中的位置，当某次没有点击到节点时重新调整为-1
	private int XBeforeDrag;// 用来表示做一次连贯的拖拽事件之前鼠标的位置，连贯是指拖拽过程中没有释放鼠标
	private int YBeforeDrag;// 用来表示做一次连贯的拖拽事件之前鼠标的位置
	private boolean add_Clicked = false, delete_Clicked = false, combine_Clicked = false, selectRoot_Clicked = false;// add,delete,modify,combine按钮的状态
	private JButton add, delete, combine, selectRoot;

	public TreePanel()
	{

		// selectedNodes = new Vector<Integer>();
		setBackground(Color.WHITE);
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent me)
			{
				mouseX = me.getX();
				mouseY = me.getY();
				tempX = me.getX();// 拖拽的中一直更改，把拖拽事件分解成小的拖拽事件，每次只移动一个像素
				tempY = me.getY();

				XBeforeDrag = me.getX();// 拖拽过程中不更改，在拖拽事件一开始就生成
				YBeforeDrag = me.getY();

				int i = 0;

				for (; i < nodes.size(); i++)
				{
					// System.out.println("--------"+mouseX+" "+mouseY);
					if (nodes.get(i).getBounds().contains(mouseX, mouseY))
					{// 鼠标停留在节点i的矩形里面
						selectedNodes = i;
						// if (me.getClickCount() ==
						// 2&&!me.isAltDown()&&!me.isControlDown()&&!me.isShiftDown()) {//双击编辑节点
						if (me.getClickCount() == 2 && !me.isAltDown())
						{// 双击编辑节点
							editingNode = i;
							nodes.get(i).setValue(nodes.get(editingNode).getValue());

						}
						if (editingNode != i)
						{
							editingNode = -1;
						}

						// if( ( me.isAltDown()&&!me.isControlDown() ) || isAdd_Clicked())
						// {//alt加点击i节点,被选中节点会增加一个子节点
						if (me.isAltDown() || isAdd_Clicked())
						{// alt加点击i节点,被选中节点会增加一个子节点
							parentOfAddingNode = selectedNodes;
							add_Clicked = false;
							add.setBackground((Color) new ColorUIResource(238, 238, 238));
							// add.setBackground(Color.YELLOW);
						}
						if (parentOfAddingNode != i)
						{
							parentOfAddingNode = -1;
						}

						// 将选择的节点设置为该树的根节点
						if (isSelectRoot_Clicked())
						{
							int indexOfPreRoot = treeLists.indexOf(nodes.get(selectedNodes).getRoot());
							treeLists.set(indexOfPreRoot, nodes.get(selectedNodes).changeRoot());
							nodes = TreePanelNode.nodesOfAllTrees(treeLists);
							selectRoot_Clicked = false;
							selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
							selectedNodes = -1;
							changeStatus_PanelModified();
							repaint();
						}
						// 鼠标点击了combine按钮，该按钮呈选中状态,连接两个节点
						if (isCombine_Clicked())
						{
							count++;
							count = count % 2;

							combinedNodes[count] = selectedNodes;
							if (combinedNodes[0] != -1 && combinedNodes[1] != -1
									&& combinedNodes[0] != combinedNodes[1])
							{// 此时表示连接两个节点

								if (!nodes.get(combinedNodes[count % 2]).getRoot()
										.equals(nodes.get(combinedNodes[(count + 1) % 2]).getRoot()))
								{// 两个节点不在同一棵树上
									TreePanelNode firstNode = nodes.elementAt(combinedNodes[(count + 1) % 2]);
									TreePanelNode secondNode = nodes.get(combinedNodes[count % 2]);

									if (secondNode.equals(secondNode.getRoot()) == false)
									{
										if (JOptionPane.showConfirmDialog(null, "连接该节点将会对树的结构进行修改，是否连接？", "确认对话框",
												JOptionPane.YES_NO_OPTION,
												JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION)
										{
											treeLists.remove(secondNode.getRoot());
											secondNode.changeRoot();
											for (TreePanelNode aNode : TreePanelNode.allNodes(secondNode))
											{
												aNode.setRoot(firstNode.getRoot());
											}
											secondNode.setParent(firstNode);
											firstNode.getChildren().add(secondNode);
											secondNode.calculateAngle();
											firstNode.sortByAngle();
											nodes = TreePanelNode.nodesOfAllTrees(treeLists);

											changeStatus_PanelModified();

											combinedNodes[0] = -1;
											combinedNodes[1] = -1;
											count = -1;
											selectedNodes = -1;
										}
										else
										{
											combinedNodes[0] = -1;
											combinedNodes[1] = -1;
											count = -1;
											selectedNodes = -1;
										}

									}
									else
									{
										treeLists.remove(secondNode.getRoot());
										for (TreePanelNode aNode : TreePanelNode.allNodes(secondNode))
										{
											aNode.setRoot(firstNode.getRoot());
										}
										secondNode.setParent(firstNode);
										firstNode.getChildren().add(secondNode);
										secondNode.calculateAngle();
										firstNode.sortByAngle();
										nodes = TreePanelNode.nodesOfAllTrees(treeLists);
										changeStatus_PanelModified();
										combinedNodes[0] = -1;
										combinedNodes[1] = -1;
										count = -1;
										selectedNodes = -1;
									}

								}
								else
								{
									combinedNodes[0] = selectedNodes;
									combinedNodes[1] = -1;
									count = 0;
								}
							}
						}
						else
						{
							combinedNodes[0] = -1;
							combinedNodes[1] = -1;
							count = -1;
						}

						if (isDelete_Clicked())
						{ // 点击了删除按钮，删除点击的节点
							for (TreePanelNode child : nodes.get(selectedNodes).getChildren())
							{
								Vector<TreePanelNode> nodesOfTree_child = TreePanelNode.allNodes(child);
								child.setParent(null);
								child.setRoot(child);
								child.calculateAngle();// 根节点的angle暂定为0
								for (int k = 1; k < nodesOfTree_child.size(); k++)
								{
									nodesOfTree_child.get(k).setRoot(child);
								}
								treeLists.add(child);// 将由该节点产生的树都加入到treeLists中

							}
							// nodes.get(selectedNodes).setChildren(new Vector<TreePanelNode>()
							// );//将该节点的子节点去掉
							if (nodes.get(selectedNodes) == nodes.get(selectedNodes).getRoot())
							{
								treeLists.remove(nodes.get(selectedNodes));
							}
							else
							{
								nodes.get(selectedNodes).getParent().getChildren().remove(nodes.get(selectedNodes));// 删除该节点
							}

							nodes = TreePanelNode.nodesOfAllTrees(treeLists);

							changeStatus_PanelModified();

							selectedNodes = -1;
							editingNode = -1;// 编辑过程中可以直接删除该节点
							System.out.println("删除了该节点");
							repaint();
						}

						break;
					}
					else
					{// 节点i不是选中节点
						selectedNodes = -1;
						editingNode = -1;
						parentOfAddingNode = -1;
					}
				}

				if (selectedNodes == -1 && parentOfAddingNode == -1)
				{// 没有选中节点，用来添加一颗新的树
					// if( ( me.isAltDown() && !me.isControlDown() ) || isAdd_Clicked() ) {
					if (me.isAltDown() || isAdd_Clicked())
					{
						parentOfAddingNode = -2;
						add_Clicked = false;
						add.setBackground((Color) new ColorUIResource(238, 238, 238));
					}
				}

				repaint();

			}

			public void mouseReleased(MouseEvent me)
			{// 该事件在点击收回鼠标时或者拖拽结束释放鼠标时触发
				parentOfAddingNode = -1;
				System.out.println("selectedNodes=" + selectedNodes);

				if ((selectedNodes != -1) && (((me.getX() - XBeforeDrag) != 0) || ((me.getY() - YBeforeDrag) != 0)))
				{// 表示发生了拖拽事件，跟新nodes,调用repaint
					if (nodes.get(selectedNodes).hasChangeAfterMoveNode(mouseX, mouseY))// 做了修改
						changeStatus_PanelModified();

					nodes.get(selectedNodes).calculateAngle();// 计算被移动节点与父节点的角度
					nodes.get(selectedNodes).calculateAngleOfChildren();// 计算被移动节点的子节点与被移动节点之间的角度
					nodes.get(selectedNodes).sortByAngle();// 将被移动节点的所有子节点排序
					if (nodes.get(selectedNodes).getParent() != null
							&& nodes.get(selectedNodes).getParent().getChildren().size() != 1)
					{// 如果被移动的节点有兄弟节点
						TreePanelNode theMovingNode = nodes.get(selectedNodes);
						Vector<TreePanelNode> siblingOfMovedNode = theMovingNode.getParent().getChildren();// 获得包括移动节点的其兄弟节点的Vector
						siblingOfMovedNode.remove(theMovingNode);// 将被移动节点从其兄弟节点序列中移除，在重新按合理顺序添加进去并且跟新nodes序列
						int i = 0;
						for (; i < siblingOfMovedNode.size(); i++)
						{// 将被移动节点所在的Vector重新按照k值大小排列
							if (theMovingNode.getAngle() > siblingOfMovedNode.get(i).getAngle())
							{
								siblingOfMovedNode.insertElementAt(theMovingNode, i);
								nodes = TreePanelNode.nodesOfAllTrees(treeLists);
								selectedNodes = nodes.indexOf(siblingOfMovedNode.elementAt(i));
								break;
							}
						}
						if (i == siblingOfMovedNode.size())
							siblingOfMovedNode.add(theMovingNode);
						nodes = TreePanelNode.nodesOfAllTrees(treeLists);
						selectedNodes = nodes.indexOf(siblingOfMovedNode.elementAt(i));
					}
					nodes = TreePanelNode.nodesOfAllTrees(treeLists);

					// 测试
					// int count = -1;
					// for (TreePanelNode theNode : nodes) {
					// count++;
					// System.out.print("count:" + count + theNode.getValue() + "的x,y为：" +
					// theNode.getX() + ","
					// + theNode.getY() + "角度是" + theNode.getAngle() + "firstChild:");
					// if (!theNode.getChildren().isEmpty())
					// System.out.println("firstChlild:" +
					// theNode.getChildren().get(0).getValue().toString());
					// }

					repaint();
				}

				if (selectedNodes == -1 && !selectedALines.isEmpty())
				{// 删除连线
					for (int selectedALine : selectedALines)
					{
						nodes.get(selectedALine).relieveRelationship();
						treeLists.add(nodes.get(selectedALine));

					}
					nodes = TreePanelNode.nodesOfAllTrees(treeLists);
					for (TreePanelNode aNode : nodes)
						aNode.print();
					selectedALines = new ArrayList<Integer>();
					changeStatus_PanelModified();
					repaint();
				}
			}
		});

		// 键盘输入监听器
		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent ke)
			{// 该方法早于keyType
				// if(ke.isControlDown()||ke.isAltDown()) {
				if (ke.isAltDown())
				{
					add_Clicked = false;
					delete_Clicked = false;
					combine_Clicked = false;
					selectRoot_Clicked = false;
					initCombineNodes();
					// System.out.println("308"+1313465465);
					add.setBackground((Color) new ColorUIResource(238, 238, 238));
					delete.setBackground((Color) new ColorUIResource(238, 238, 238));
					combine.setBackground((Color) new ColorUIResource(238, 238, 238));
					selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));

				}
				if (ke.getKeyCode() == KeyEvent.VK_DELETE)
				{
					add_Clicked = false;
					delete_Clicked = false;
					combine_Clicked = false;
					selectRoot_Clicked = false;
					initCombineNodes();
					// System.outprintln("308"+1313465465);
					add.setBackground((Color) new ColorUIResource(238, 238, 238));
					delete.setBackground((Color) new ColorUIResource(238, 238, 238));
					combine.setBackground((Color) new ColorUIResource(238, 238, 238));
					selectRoot.setBackground((Color) new ColorUIResource(238, 238, 238));
				}
				// System.out.println("+++++");
				if (selectedNodes != -1 && ke.getKeyCode() == KeyEvent.VK_DELETE)
				{
					for (TreePanelNode child : nodes.get(selectedNodes).getChildren())
					{
						Vector<TreePanelNode> nodesOfTree_child = TreePanelNode.allNodes(child);
						child.setParent(null);
						child.setRoot(child);
						child.calculateAngle();// 根节点的angle暂定为0
						for (int k = 1; k < nodesOfTree_child.size(); k++)
						{
							nodesOfTree_child.get(k).setRoot(child);
						}
						treeLists.add(child);// 将由该节点产生的树都加入到treeLists中

					}
					// nodes.get(selectedNodes).setChildren(new Vector<TreePanelNode>()
					// );//将该节点的子节点去掉
					if (nodes.get(selectedNodes) == nodes.get(selectedNodes).getRoot())
					{
						treeLists.remove(nodes.get(selectedNodes));
					}
					else
					{
						nodes.get(selectedNodes).getParent().getChildren().remove(nodes.get(selectedNodes));// 删除该节点
					}

					nodes = TreePanelNode.nodesOfAllTrees(treeLists);

					changeStatus_PanelModified();
					selectedNodes = -1;
					editingNode = -1;// 编辑过程中可以直接删除该节点
					System.out.println("删除了该节点");
					repaint();
				}

				if (editingNode != -1 && ke.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				{// 当点了backspace时 可以删除字符
					String str = nodes.get(editingNode).getValue().toString() + String.valueOf(ke.getKeyChar()).trim();
					if (str.length() != 0)
					{
						str = str.substring(0, str.length() - 1);
						nodes.get(editingNode).setValue(str);
						changeStatus_PanelModified();
					}
				}
				if (editingNode != -1 && ke.getKeyCode() == KeyEvent.VK_ENTER)
				{// 输入enter表示输入完成
					editingNode = -1;
					changeStatus_PanelModified();
				}

			}

			public void keyTyped(KeyEvent ke)
			{// 除了Shift、Ctrl、alt之外输入任一个字符均会调用该方法
				// System.out.println("====");
				if (editingNode != -1)
				{
					nodes.get(editingNode).setValue(
							nodes.get(editingNode).getValue().toString() + String.valueOf(ke.getKeyChar()).trim());
					changeStatus_PanelModified();
					repaint();
				}
			}

		});

		addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseDragged(MouseEvent me)
			{// 该事件不会触发mouseMoved
				mouseX = me.getX();
				mouseY = me.getY();
				System.out.println("拖拽了");
				// 确定鼠标停留在所选择节点的上面
				if (selectedNodes != -1)
				{

					// if (me.isAltDown() && !me.isControlDown()&&parentOfAddingNode!=-1) {
					if (me.isAltDown() && parentOfAddingNode != -1)
					{
						parentOfAddingNode = selectedNodes;
						// System.out.println("添加了"+parentOfAddingNode+"的子节点");
					}
					else
					{
						parentOfAddingNode = -1;

						if (nodes.get(selectedNodes).hasChangeAfterMoveNode(mouseX, mouseY))// 做了修改
							changeStatus_PanelModified();
						// 用来将选中的节点移动,移动节点
						nodes.get(selectedNodes).setLocation(nodes.get(selectedNodes).getLocation().x + mouseX - tempX,
								nodes.get(selectedNodes).getLocation().y + mouseY - tempY);

						tempX = mouseX;
						tempY = mouseY;

						System.out.println("移动了第" + selectedNodes + "个节点");
					}
					repaint();
				}
				else if (isDelete_Clicked())
				{// 删除节点之间的连线
					selectedALines = new ArrayList<Integer>();
					for (int i = 0; i < nodes.size(); i++)
					{
						if (nodes.get(i).isLineSelected(XBeforeDrag, YBeforeDrag, me.getX(), me.getY()))
						{
							selectedALines.add(i);
						}
					}
					repaint();
				}
			}

			public void mouseMoved(MouseEvent me)
			{// 鼠标没有点击时的移动
				mouseX = me.getX();
				mouseY = me.getY();
				// System.out.println(mouseX+" "+mouseY);
				// System.out.println("mouseMoved");
			}
		});
	}

	public void changeStatus_PanelModified()
	{// 将txtPath的文件状态修改为“已经被修改，还未保存”状态
		String txtPath = this.getTreeAtTxt().getTxtPath();
		hasModeified.put(txtPath, Boolean.TRUE);
	}

	public Vector<TreePanelNode> getNodes()
	{
		return nodes;
	}

	public void setNodes(Vector<TreePanelNode> nodes)
	{
		this.nodes = nodes;
	}

	public ArrayList<TreePanelNode> getTreeLists()
	{
		return treeLists;
	}

	public void setTreeLists(ArrayList<TreePanelNode> treeLists)
	{
		this.treeLists = treeLists;
	}

	public boolean isAdd_Clicked()
	{
		return add_Clicked;
	}

	public void setAdd_Clicked(boolean add_Clicked)
	{
		this.add_Clicked = add_Clicked;
	}

	public boolean isDelete_Clicked()
	{
		return delete_Clicked;
	}

	public void setDelete_Clicked(boolean delete_Clicked)
	{
		this.delete_Clicked = delete_Clicked;
	}

	// public boolean isModify_Clicked() {
	// return modify_Clicked;
	// }
	//
	//
	// public void setModify_Clicked(boolean modify_Clicked) {
	// this.modify_Clicked = modify_Clicked;
	// }

	public boolean isCombine_Clicked()
	{
		return combine_Clicked;
	}

	public void setCombine_Clicked(boolean combine_Clicked)
	{
		this.combine_Clicked = combine_Clicked;
	}

	public JButton getAdd()
	{
		return add;
	}

	public void setAdd(JButton add)
	{
		this.add = add;
	}

	public JButton getDelete()
	{
		return delete;
	}

	public void setDelete(JButton delete)
	{
		this.delete = delete;
	}

	public JButton getCombine()
	{
		return combine;
	}

	public void setCombine(JButton combine)
	{
		this.combine = combine;
	}

	public boolean isSelectRoot_Clicked()
	{
		return selectRoot_Clicked;
	}

	public void setSelectRoot_Clicked(boolean selectRoot_Clicked)
	{
		this.selectRoot_Clicked = selectRoot_Clicked;
	}

	public JButton getSelectRoot()
	{
		return selectRoot;
	}

	public void setSelectRoot(JButton selectRoot)
	{
		this.selectRoot = selectRoot;
	}

	public int getSelectedNodes()
	{
		return selectedNodes;
	}

	public void setSelectedNodes(int selectedNodes)
	{
		this.selectedNodes = selectedNodes;
	}

	public int getSelectedALine()
	{
		return selectedALine;
	}

	public void setSelectedALine(int selectedALine)
	{
		this.selectedALine = selectedALine;
	}

	public void initCombineNodes()
	{
		combinedNodes[0] = -1;
		combinedNodes[1] = -1;
		count = -1;
	}

	public TreeAtTxt getTreeAtTxt()
	{
		return treeAtTxt;
	}

	public void setTreeAtTxt(TreeAtTxt treeAtTxt)
	{
		this.treeAtTxt = treeAtTxt;
	}

	public HashMap<String, Boolean> getHasModeified()
	{
		return hasModeified;
	}

	public void setHasModeified(HashMap<String, Boolean> hasModeified)
	{
		this.hasModeified = hasModeified;
	}

	public static void drawAL(int sx, int sy, int ex, int ey, Graphics2D g2)
	{

		double H = 10; // 箭头高度
		double L = 4; // 底边的一半
		int x3 = 0;
		int y3 = 0;
		int x4 = 0;
		int y4 = 0;
		double awrad = Math.atan(L / H); // 箭头角度
		double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
		double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
		double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
		double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点
		double y_3 = ey - arrXY_1[1];
		double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点
		double y_4 = ey - arrXY_2[1];

		Double X3 = new Double(x_3);
		x3 = X3.intValue();
		Double Y3 = new Double(y_3);
		y3 = Y3.intValue();
		Double X4 = new Double(x_4);
		x4 = X4.intValue();
		Double Y4 = new Double(y_4);
		y4 = Y4.intValue();
		// 画线
		g2.drawLine(sx, sy, ex, ey);
		//
		GeneralPath triangle = new GeneralPath();
		triangle.moveTo(ex, ey);
		triangle.lineTo(x3, y3);
		triangle.lineTo(x4, y4);
		triangle.closePath();
		// 实心箭头
		// g2.fill(triangle);
		// 非实心箭头
		g2.draw(triangle);

	}

	// 计算
	public static double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen)
	{

		double mathstr[] = new double[2];
		// 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
		double vx = px * Math.cos(ang) - py * Math.sin(ang);
		double vy = px * Math.sin(ang) + py * Math.cos(ang);
		if (isChLen)
		{
			double d = Math.sqrt(vx * vx + vy * vy);
			vx = vx / d * newLen;
			vy = vy / d * newLen;
			mathstr[0] = vx;
			mathstr[1] = vy;
		}
		return mathstr;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		FontMetrics fm = g.getFontMetrics();
		String str = "newNode";
		int width = fm.stringWidth(str) + 20;
		int height = fm.getHeight() + 20;

		// 添加新节点
		if (parentOfAddingNode != -1 && parentOfAddingNode != -2)
		{// 添加一个树叶
			TreePanelNode newNode = new TreePanelNode(nodes.get(parentOfAddingNode), mouseX, mouseY, width, height,
					"newNode");
			newNode.setRoot(nodes.get(parentOfAddingNode).getRoot());// 为新增节点添加root节点
			newNode.calculateAngle();// 为新增节点求得k值
			double kOfNewNode = newNode.getAngle();
			Vector<TreePanelNode> siblingOfNewNode = nodes.get(parentOfAddingNode).getChildren();
			int j;

			for (j = 0; j < siblingOfNewNode.size(); j++)
			{// 将新增节点插入到其父节点的vector<>children序列中的适当位置
				if (kOfNewNode > siblingOfNewNode.get(j).getAngle())
				{
					siblingOfNewNode.insertElementAt(newNode, j);
					nodes = TreePanelNode.nodesOfAllTrees(treeLists);
					selectedNodes = nodes.indexOf(newNode);

					// 测试
					for (int i = 0; i < nodes.size(); i++)
					{
						System.out.println(nodes.get(i).getValue() + "节点的斜率：" + nodes.get(i).getAngle());
					}

					break;
				}
			}
			if (j == siblingOfNewNode.size())
			{
				siblingOfNewNode.insertElementAt(newNode, j);
				nodes = TreePanelNode.nodesOfAllTrees(treeLists);
				selectedNodes = nodes.indexOf(newNode);

				// 测试
				for (int i = 0; i < nodes.size(); i++)
				{
					System.out.println(nodes.get(i).getValue() + "节点的斜率：" + nodes.get(i).getAngle());
				}

			}
			changeStatus_PanelModified();
			parentOfAddingNode = -1;
		}

		if (parentOfAddingNode == -2)
		{// 增加一棵树
			TreePanelNode newNode = new TreePanelNode(null, mouseX, mouseY, width, height, "newNode");
			newNode.setRoot(newNode);// 为新增节点添加root节点
			newNode.calculateAngle();// 为新增节点求得k值
			treeLists.add(newNode);
			// System.out.println("treeLists为空："+treeLists.isEmpty());
			nodes = TreePanelNode.nodesOfAllTrees(treeLists);
			// System.out.println("nodes为空："+nodes.isEmpty());
			selectedNodes = nodes.indexOf(newNode);
			changeStatus_PanelModified();
			parentOfAddingNode = -1;
		}

		for (int i = 0; i < nodes.size(); i++)
		{// repaint被调用该for循环必然被调用
			{
				if (selectedNodes == i)
					g.setColor(Color.RED);
				else
					g.setColor(Color.BLACK);
			}

			{// 根据所有的节点以Vector序列nodes画出节点
				String value = nodes.get(i).getValue().toString();
				int wide = fm.stringWidth(value) + 20;
				int high = fm.getHeight() + 20;
				nodes.get(i).setSize(wide, high);// 根据节点中内容设定节点的宽高

				// g.drawLine(nodes.get(i).getLocation().x + nodes.get(i).getWidth() / 2,
				// nodes.get(i).getLocation().y + nodes.get(i).getHeight() / 2,
				// nodes.get(i).getParent().getLocation().x +
				// nodes.get(i).getParent().getWidth() / 2,
				// nodes.get(i).getParent().getLocation().y );
				g.drawString(value, nodes.get(i).getLocation().x + 10, nodes.get(i).getLocation().y + 20);
				g.drawRect(nodes.get(i).getLocation().x, nodes.get(i).getLocation().y, nodes.get(i).getWidth(),
						nodes.get(i).getHeight());

			}
		}

		for (int i = 0; i < nodes.size(); i++)
		{
			if (selectedALines.contains(i))
				g.setColor(Color.RED);
			else
				g.setColor(Color.BLACK);
			if (nodes.get(i).getParent() != null)
				drawAL(nodes.get(i).getParent().getLocation().x + nodes.get(i).getParent().getWidth() / 2,
						nodes.get(i).getParent().getLocation().y + nodes.get(i).getParent().getHeight(),
						nodes.get(i).getLocation().x + nodes.get(i).getWidth() / 2, nodes.get(i).getLocation().y,
						(Graphics2D) g);
		}

		grabFocus();// 用来获取焦点，否则keylistener不响应
	}

}
