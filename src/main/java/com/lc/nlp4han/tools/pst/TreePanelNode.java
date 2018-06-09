package com.lc.nlp4han.tools.pst;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;

public class TreePanelNode
{
	private Vector<TreePanelNode> children;
	private TreePanelNode parent;
	private TreePanelNode root;
	
	private int x, y, width, height;
	
	private Object value;
	
	private double angle;// 该节点与父节点之间的“夹角”，根节点不用计算

	// 测试用构造函数
	public TreePanelNode(TreePanelNode parent, int x, int y, int width, int height, Object value)
	{
		this.x = x;
		this.y = y;
		this.parent = parent;
		this.width = width;
		this.height = height;
		this.value = value;
		this.children = new Vector<TreePanelNode>();
		if (parent == null)
			this.root = this;
		else
		{
			this.root = parent.getRoot();
		}
	}

	public TreePanelNode()
	{
	}

	/*
	 *
	 * 判断树的叶子节点是否均没有兄弟节点(在导出括号表达式时不允许叶子节点有兄弟节点) 若树上只有两个节点，则该树也不符合条件
	 */
	public boolean examTheTree()
	{
		TreePanelNode root = this.getRoot();
		if (allNodes(root).size() == 2)
			return false;
		for (TreePanelNode node : allNodes(root))
		{
			if (node.getChildren().isEmpty())
				if (node.hasSibling())
				{
					return false;
				}
		}
		return true;

	}

	// 判断任意一个节点是否有兄弟节点
	public boolean hasSibling()
	{
		if (this.equals(this.getRoot()))
			return false;
		if (this.getParent().getChildren().size() != 1)
			return true;
		else
			return false;
	}

	public static Vector<TreePanelNode> nodesOfAllTrees(ArrayList<TreePanelNode> treeLists)
	{
		Vector<TreePanelNode> nodes = new Vector<TreePanelNode>();
		int i = 0;
		for (TreePanelNode aRoot : treeLists)
		{// 每循环一次就将一个树上所有的节点放到nodes中，i表示nodes的节点个数
			if (aRoot == null)
				continue;

			nodes.add(aRoot);
			for (; i < nodes.size(); i++)
			{
				if (nodes.get(i).children.size() != 0)
				{
					for (TreePanelNode child : nodes.get(i).children)
					{
						nodes.add(child);
					}
				}
			}
		}
		return nodes;
	}

	// 返回以root节点为根节点的所有节点的Vector序列，可以通过层序遍历得到
	public static Vector<TreePanelNode> allNodes(TreePanelNode root)
	{
		Vector<TreePanelNode> nodes = new Vector<TreePanelNode>();
		if (root == null)
			return nodes;
		else
		{
			nodes.add(root);
			int i = 0;
			for (; i < nodes.size(); i++)
			{
				if (nodes.get(i).children.size() != 0)
				{
					for (TreePanelNode child : nodes.get(i).children)
					{
						nodes.add(child);
					}
				}
			}
			return nodes;
		}
	}

	// 给定一个节点，将该组节点的子节点按照其angle的大小从高到低排列

	public Vector<TreePanelNode> sortByAngle()
	{
		int length = children.size();
		// System.out.println(length);
		if (length != 0)
		{
			int i = 1;
			for (; i < length; i++)
			{
				for (int j = 0; j < length - i; j++)
				{
					if (children.elementAt(j).getAngle() < children.elementAt(j + 1).getAngle())
					{
						TreePanelNode temp = children.elementAt(j);
						children.set(j, children.elementAt(j + 1));
						children.set(j + 1, temp);
					}
				}
			}
		}
		return children;
	}

	// 计算该节点与父节点之间的“夹角”
	// 父节点的孩子节点存放在Vector中，Vector中序号小的节点是兄长节点,
	// 兄长节点大于兄弟节点与父节点的"夹角"，夹角从pi/2到-3pi/2.由于y轴向下，故该夹角与正常的夹角都有些不一样。
	// 子节点在以父节点为原点的正常坐标轴的第二象限时，夹角为pi/2->0;在第三象限，夹角为0->-pi/2;在第四象限，夹角为-pi/2->-pi;在第一象限，夹角为-pi->-3pi/2.
	// 夹角大小表示一种先后关系
	public void calculateAngle()
	{
		if (this.parent != null)
		{
			if ((this.x + this.width / 2) != (this.parent.x + this.parent.width / 2))
			{
				double xChild = this.x + this.width / 2;
				double yChild = this.y + this.height / 2;
				double xParent = this.parent.x + this.parent.width / 2;
				double yParent = this.parent.y + this.parent.height / 2;
				if (xChild < xParent && yChild <= yParent)
					this.angle = Math.atan((yParent - yChild) / (xParent - xChild));
				if (xChild < xParent && yChild > yParent)
					this.angle = Math.atan((yParent - yChild) / (xParent - xChild));
				if (xChild > xParent && yChild >= yParent)
					this.angle = Math.atan((yParent - yChild) / (xParent - xChild)) - Math.PI;
				if (xChild > xParent && yChild < yParent)
					this.angle = Math.atan((yParent - yChild) / (xParent - xChild)) - Math.PI;
			}
			else if (y + this.height / 2 > this.parent.y + this.parent.height / 2)
				this.angle = -Math.PI / 2;
			else
				this.angle = -3 * Math.PI / 2;
		}
		else
		{
			this.angle = 0;// 根节点的angle暂时定位0
		}
	}

	/*
	 * 计算该节点的所有子节点与该节点的角度
	 */
	public void calculateAngleOfChildren()
	{
		int length = this.children.size();
		if (length != 0)
		{
			for (int i = 0; i < length; i++)
			{
				children.elementAt(i).calculateAngle();
			}
		}
	}

	public boolean hasChangeAfterMoveNode(int xAfterMoved, int yAfterMoved)
	{
		if (allNodes(root).size() <= 2)
			return false;
		if (getParent() != null && !getChildren().isEmpty() && getParent().getChildren().size() >= 2)
		{// 有兄弟节点
			// 判断是否真的修改
			double angelAfterMoved = 0;
			if ((xAfterMoved + this.width / 2) != (this.parent.x + this.parent.width / 2))
			{// 计算移动后与父节点的角度，没有将数据结构跟新，只是简单计算值
				double xChild = xAfterMoved + this.width / 2;
				double yChild = yAfterMoved + this.height / 2;
				double xParent = this.parent.x + this.parent.width / 2;
				double yParent = this.parent.y + this.parent.height / 2;
				if (xChild < xParent && yChild <= yParent)
					angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild));
				if (xChild < xParent && yChild > yParent)
					angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild));
				if (xChild > xParent && yChild >= yParent)
					angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild)) - Math.PI;
				if (xChild > xParent && yChild < yParent)
					angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild)) - Math.PI;
			}
			else if (yAfterMoved + this.height / 2 > this.parent.y + this.parent.height / 2)
				angelAfterMoved = -Math.PI / 2;
			else
				angelAfterMoved = -3 * Math.PI / 2;
			int count = 0;
			int i = 0;
			for (; i < getChildren().size(); i++)
			{
				if (angelAfterMoved >= getChildren().get(i).getAngle())
				{
					count = i;
					break;
				}
			}
			if (i == getChildren().size())
				count = i;
			if (angelAfterMoved == angle)
				return false;
			else if (angelAfterMoved > angle)
			{
				return (children.indexOf(this) + 1 - count) >= 2 ? true : false;
			}
			else
				return (count - children.indexOf(this)) >= 2 ? true : false;

		}
		else if (getChildren().size() >= 2)
		{// 节点有两个以上儿子
			// 判断是否修改
			ArrayList<Double> angles = new ArrayList<Double>();
			for (TreePanelNode son : children)
			{
				double angelAfterMoved = 0;
				if ((son.x + son.width / 2) != (xAfterMoved + this.width / 2))
				{// 计算移动后与父节点的角度，没有将数据结构跟新，只是简单计算值
					double xChild = son.x + son.width / 2;
					double yChild = son.y + son.height / 2;
					double xParent = xAfterMoved + this.width / 2;
					double yParent = yAfterMoved + this.height / 2;
					if (xChild < xParent && yChild <= yParent)
						angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild));
					if (xChild < xParent && yChild > yParent)
						angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild));
					if (xChild > xParent && yChild >= yParent)
						angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild)) - Math.PI;
					if (xChild > xParent && yChild < yParent)
						angelAfterMoved = Math.atan((yParent - yChild) / (xParent - xChild)) - Math.PI;
				}
				else if (son.y + son.height / 2 > yAfterMoved + this.height / 2)
					angelAfterMoved = -Math.PI / 2;
				else
					angelAfterMoved = -3 * Math.PI / 2;
				angles.add(angelAfterMoved);
			}
			for (int i = 0; i < angles.size() - 1; i++)
			{
				if (angles.get(i) - angles.get(i + 1) < 0.00000001)
					return true;
			}
			return false;

		}
		return false;
	}

	/*
	 * 
	 * 删除该节点与其父节点的关系，使得该节点成为新树的根节点
	 * 
	 */
	public void relieveRelationship()
	{
		if (this != this.root)
		{
			this.parent.getChildren().remove(this);
			this.angle = 0;
			this.parent = null;
			for (TreePanelNode aNode : allNodes(this))
			{
				aNode.setRoot(this);
			}
		}
	}

	/*
	 * 
	 * 
	 * 判断该节点与其父节点的连线是否被选中
	 * 
	 */
	public boolean isLineSelected2(int X1, int Y1, int X2, int Y2)
	{// 利用矩形
		if (this == this.root)
			return false;
		if (Math.min(X1, X2) <= Math.max(x + width / 2, parent.x + parent.width / 2)
				&& Math.max(X1, X2) >= Math.min(x + width / 2, parent.x + parent.width / 2)
				&& Math.min(Y1, Y2) <= Math.max(y, parent.y + parent.height)
				&& Math.max(Y1, Y2) >= Math.min(y, parent.y + parent.height))
		{
			return true;
		}
		else
			return false;
	}

	public boolean isLineSelected(int X1, int Y1, int X2, int Y2)
	{// 两个线段有交点
		if (this == this.root)
			return false;
		if (!(Math.min(X1, X2) <= Math.max(x + width / 2, parent.x + parent.width / 2)
				&& Math.max(X1, X2) >= Math.min(x + width / 2, parent.x + parent.width / 2)
				&& Math.min(Y1, Y2) <= Math.max(y, parent.y + parent.height)
				&& Math.max(Y1, Y2) >= Math.min(y, parent.y + parent.height)))
		{
			return false;
		}
		else
		{
			double u = (X1 - (parent.x + parent.width / 2)) * (y - (parent.y + parent.height))
					- (x + width / 2 - (parent.x + parent.width / 2)) * (Y1 - (parent.y + parent.height));
			double v = (X2 - (parent.x + parent.width / 2)) * (y - (parent.y + parent.height))
					- (x + width / 2 - (parent.x + parent.width / 2)) * (Y2 - (parent.y + parent.height));
			double w = (parent.x + parent.width / 2 - X1) * (Y2 - Y1) - (X2 - X1) * (parent.y + parent.height - Y1);
			double z = (x + width / 2 - X1) * (Y2 - Y1) - (X2 - X1) * (y - Y1);

			if (u * v <= 0.00000001 && w * z <= 0.00000001)
				return true;
			else
				return false;
		}

	}

	/*
	 * 将选中节点修改为该节点所在树的根节点
	 */
	public TreePanelNode changeRoot()
	{
		TreePanelNode preRoot = this.root;
		if (this.equals(this.root))
			return preRoot;
		// 所有节点均先修改其root值

		for (TreePanelNode node : allNodes(preRoot))
			node.setRoot(this);
		// 1.修改被选中节点
		this.setAngle(0);
		this.children.add(this.parent);

		// 2.修改被选节点与原root路径上所有（不包括被选节点及原root）的节点
		TreePanelNode cursor = this.getParent(), begin, preParent;
		begin = this;

		for (; !cursor.equals(preRoot); begin = cursor, cursor = preParent)
		{
			preParent = cursor.getParent();
			cursor.setParent(begin);
			cursor.getChildren().remove(begin);
			cursor.getChildren().add(preParent);
			cursor.calculateAngle();
			cursor.sortByAngle();
		}
		this.setParent(null);
		this.sortByAngle();

		// 3.修改原root
		preRoot.setParent(begin);
		preRoot.getChildren().remove(begin);
		preRoot.calculateAngle();
		preRoot.sortByAngle();

		// 其他节点均只需要修改其root

		return this;
	}

	/*
	 * 对树进行先根遍历得到一个序列
	 */
	public Vector<TreePanelNode> traverse_rootFisrt(TreePanelNode theNode, Vector<TreePanelNode> nodes_rootFirst)
	{
		nodes_rootFirst.add(theNode);
		if (theNode.getChildren().size() != 0)
		{
			for (TreePanelNode child : theNode.getChildren())
			{
				traverse_rootFisrt(child, nodes_rootFirst);
			}
		}

		return nodes_rootFirst;
	}

	/*
	 * 判断该叶子节点是树上按先根遍历的第几个叶子节点
	 */
	public int getIndexOfLeaf(TreePanelNode leaf)
	{
		int count = 0;
		TreePanelNode rootOfLeaf = leaf.getRoot();
		if (leaf.children.size() != 0)
			return 0;
		Vector<TreePanelNode> nodes_rootFirst = new Vector<TreePanelNode>();
		nodes_rootFirst = traverse_rootFisrt(rootOfLeaf, nodes_rootFirst);
		for (TreePanelNode theLeaf : nodes_rootFirst)
		{
			if (theLeaf.getChildren().size() == 0)
			{
				count++;
				if (theLeaf.equals(leaf))
					return count;
			}
		}
		return count;
	}

	/*
	 * 返回指定节点在树上的高度
	 */
	public int height()
	{// 根节点高度为0
		TreePanelNode cursor = this;
		int count = 0;
		while (!cursor.equals(this.root))
		{
			count++;
			cursor = cursor.parent;
		}
		return count;
	}

	/*
	 * 给指定的单颗树分配位置，不会计算角度。采用后根遍历树
	 */
	public static TreePanelNode allocatePosition(TreePanelNode root)
	{
		// if(!root.equals(root.getRoot()))
		// return null;

		if (root.getChildren().size() != 0)
		{
			int countOfChild = 0;
			int rootX = 0, rootY = 0, rootWidth = 60, rootHeight = 30;
			for (TreePanelNode child : root.getChildren())
			{
				countOfChild++;// 判断孩子的个数
				allocatePosition(child);
			}

			for (TreePanelNode child : root.getChildren())
			{
				rootX += child.getX();
			}
			root.setX(rootX / countOfChild);
			root.setY(80 * root.height());
			root.setHeight(rootHeight);
			root.setWidth(rootWidth);
		}
		else
		{
			// 该root是叶子节点
			root.setX(80 * root.getIndexOfLeaf(root));
			root.setY(80 * root.height());
			root.setHeight(30);
			root.setWidth(60);
		}
		
		return root;
	}

	/*
	 * 给指定的森林分配位置。采用后根遍历树
	 */
	public static ArrayList<TreePanelNode> repaintTreeLists(ArrayList<TreePanelNode> treeLists)
	{
		if (treeLists.size() == 0 || treeLists == null)
		{
			System.out.println(123456789);
			return new ArrayList<TreePanelNode>();
		}

		int count = 0;// 表示森林中的第count颗树
		for (TreePanelNode tree : treeLists)
		{
			if (count == 0)
				allocatePosition(tree);
			else
			{
				allocatePosition(tree);
				Vector<TreePanelNode> nodesOfTree = allNodes(treeLists.get(count - 1));
				int modificationY = nodesOfTree.get(nodesOfTree.size() - 1).getY() + 80;
				for (TreePanelNode node : allNodes(tree))
					node.setY(node.getY() + modificationY);
			}
			count++;
		}
		for (TreePanelNode node : nodesOfAllTrees(treeLists))
			node.calculateAngle();
		return treeLists;
	}

	/*
	 * 
	 * 
	 * 将一个括号表达式转化为一颗树
	 * 
	 * 
	 * 
	 */
	// 将括号表达式转化为树，并分配位置和初始化角度
	public ArrayList<TreePanelNode> fromTextToTree(String strWithFormat)
	{
		if (toOneLine(strWithFormat).trim().length() == 0)
			return null;
		ArrayList<TreePanelNode> treeLists = repaintTreeLists(getTree(stringToList(format(toOneLine(strWithFormat)))));
		for (TreePanelNode tree : treeLists)
		{
			for (TreePanelNode node : allNodes(tree))
			{
				node.calculateAngle();
			}
		}

		return treeLists;
	}

	// 将多行括号表达式转换为单行的
	public String toOneLine(String str)
	{
		return str.replaceAll("\n", "").replaceAll("\r", "");
	}

	// 处理括号表达式类似(ROOT (IP (NP (NT 去年 )) (NP (NR 耐克 )) (VP (PP (P 在) (NP (NP (DNP (NP
	// (NR 曼哈顿)) (DEG 的)) (NP (NN 潮流) (NN 聚集地))) (NP (NR 苏荷区)))) (VP (VV 开) (AS 了)
	// (NP (QP (CD 一) (CLP (M 家))) (NP (NN 新店))))) (PU 。)))
	// 处理
	// 结果(ROOT(IP(NP(NT(去年)))(NP(NR(耐克)))(VP(PP(P(在))(NP(NP(DNP(NP(NR(曼哈顿)))(DEG(的)))(NP(NN(潮流))(NN(聚集地))))(NP(NR(苏荷区)))))(VP(VV(开))(AS(了))(NP(QP(CD(一))(CLP(M(家))))(NP(NN(新店))))))(PU(。))))
	// 格式化单行括号表达式，消除其中空格
	public String format2(String str)
	{
		str = str.trim();
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == ' ')
			{
				if ((str.charAt(i - 1) == '(') || (str.charAt(i - 1) == ')') || str.charAt(i + 1) == '('
						|| str.charAt(i + 1) == ')' || str.charAt(i + 1) == ' ')
				{
					str = str.substring(0, i) + str.substring(i + 1, str.length());
					i--;
				}
				else if ((str.substring(i + 1).indexOf('(') != -1) && (str.substring(i + 1).indexOf(')') != -1)
						&& (str.substring(i + 1).indexOf(')') < str.substring(i + 1).indexOf('(')))
				{// 排除描述语句中可能出现的空格
					System.out.println("i=" + i);
					System.out.println(")=" + str.substring(i + 1).indexOf(')'));
					System.out.println("(=" + str.substring(i + 1).indexOf('('));
					System.out.println("");
					str = str.replaceFirst(" ", "(");
					for (int j = i + 1; j < str.length(); j++)
					{
						if (str.charAt(j) == ' ')
						{
							str = str.replaceFirst(" ", ")");
							i = j;
							break;
						}
						else if (str.charAt(j) == ')')
						{
							str = str.substring(0, j) + ")" + str.substring(j, str.length());
							i = j;
							break;
						}
					}
				}
				else
				{
					System.out.println("i=" + i);
					System.out.println(")=" + str.substring(i + 1).indexOf(')'));
					System.out.println("(=" + str.substring(i + 1).indexOf('('));
					System.out.println("");

				}
			}
		}
		return str;
	}

	// 处理括号表达式 类似(ROOT (IP (NP (NT 去年 )) (NP (NR 耐克 )) (VP (PP (P 在) (NP (NP (DNP
	// (NP (NR 曼哈顿)) (DEG 的)) (NP (NN 潮流) (NN 聚集地))) (NP (NR 苏荷区)))) (VP (VV 开) (AS
	// 了) (NP (QP (CD 一) (CLP (M 家))) (NP (NN 新店))))) (PU 。)))
	public String format(String str)
	{
		str = str.trim();
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == ' ')
			{
				if ((str.charAt(i - 1) == '(') || (str.charAt(i - 1) == ')') || str.charAt(i + 1) == '('
						|| str.charAt(i + 1) == ')' || str.charAt(i + 1) == ' ')
				{
					str = str.substring(0, i) + str.substring(i + 1, str.length());
					i--;
				}
				else
				{
					for (int k = i + 1; k < str.length(); k++)
					{
						if (str.charAt(k) == ')')
						{
							str = str.substring(0, i) + "(" + str.substring(i + 1);
							for (int j = i + 1; j < str.length(); j++)
							{
								if (str.charAt(j) == ' ')
								{
									str = str.replaceFirst(" ", ")");
									i = j;
									break;
								}
								else if (str.charAt(j) == ')')
								{
									str = str.substring(0, j) + ")" + str.substring(j, str.length());
									i = j;
									break;
								}
							}
							break;
						}
						else if (str.charAt(k) == '(')
							break;
					}
				}

			}
		}
		return str;
	}

	/*
	 * 将格式化后的括号表达式封装到ArrayList中 ,如: 中国篮球协会在北京市通州区张家湾镇中心小学举行了小篮球发展计划暨小篮球联赛启动仪式。
	 * (ROOT(IP(NP(NP(NR(中国)))(NP(NN(篮球))(NN(协会))))(VP(PP(P(在))(NP(NP(NR(北京市))(NR(
	 * 通州区))(NR(张家湾镇)))(NP(NN(中心))(NN(小学)))))(VP(VV(举行))(AS(了))(NP(NP(NN(小篮球))(NN(发展
	 * ))(NN(计划)))(CC(暨))(NP(NN(小篮球))(NN(联赛))(NN(启动))(NN(仪式))))(PU(。))))
	 * 小篮球规则适用对象为12岁及以下的少年儿童。
	 * (ROOT(IP(NP(NN(小篮球))(NN(规则))(NN(适用))(NN(对象)))(VP(VC(为)))(NP(DNP(QP(CD(12))(
	 * CLP(M(岁)))(CC(及))(NP(PN(以下))))(DEG(的)))(NP(NN(少年))(NN(儿童))))(PU(。))))
	 * 会将字符创str1= "中国篮球协会在北京市通州区张家湾镇中心小学举行了小篮球发展计划暨小篮球联赛启动仪式。"
	 * str2="小篮球规则适用对象为12岁及以下的少年儿童。" 也添加到list中。
	 */
	// public ArrayList<String> stringToList(String str){ //错误
	//
	// ArrayList<String> list =new ArrayList<String>();
	// char[] chars = str.toCharArray();
	// for(int i = 0, j = 0 ;i < chars.length; i++) {
	// if (chars[i] =='(' || chars[i] == ')') {
	// if(i == j) {
	// list.add(String.valueOf(chars,j,1));
	// }else {
	// if (chars[j] == '(' || chars[j] == ')') {
	// if (!String.valueOf(chars, j + 1, i - j - 1).equals(""))
	// list.add(String.valueOf(chars, j + 1, i - j - 1));
	// } else {
	// if (!String.valueOf(chars, j, i - j ).equals(""))
	// list.add(String.valueOf(chars, j + 1, i - j ));
	// }
	// j = i;
	// list.add(String.valueOf(chars, i, 1));
	// }
	// }
	// }
	// return list;
	// }
	public ArrayList<String> stringToList(String str)
	{ // 有描述和无描述均正常

		ArrayList<String> list = new ArrayList<String>();
		char[] chars = str.toCharArray();
		for (int i = 0, j = 0; i < chars.length; i++)
		{
			if (chars[i] == '(' || chars[i] == ')')
			{
				if (i == j)
				{
					list.add(String.valueOf(chars, j, 1));
				}
				else
				{
					if (chars[j] != '(' && chars[j] != ')')
					{
						list.add(String.valueOf(chars, j, i - j));
						// System.out.println("chars[j] ="+chars[j]);
						// System.out.println("String.valueOf(chars, j, i - j )"+String.valueOf(chars,
						// j, i - j ));
					}
					else if (!String.valueOf(chars, j + 1, i - j - 1).equals(""))
						list.add(String.valueOf(chars, j + 1, i - j - 1));
					list.add(String.valueOf(chars, i, 1));
					j = i;
				}
			}
		}
		return list;
	}

	public ArrayList<String> stringToList2(String treeStr)
	{

		ArrayList<String> parts = new ArrayList<String>();
		for (int index = 0; index < treeStr.length(); ++index)
		{
			if (treeStr.charAt(index) == '(' || treeStr.charAt(index) == ')' || treeStr.charAt(index) == ' ')
			{
				parts.add(Character.toString(treeStr.charAt(index)));
			}
			else
			{
				for (int i = index + 1; i < treeStr.length(); ++i)
				{
					if (treeStr.charAt(i) == '(' || treeStr.charAt(i) == ')' || treeStr.charAt(i) == ' ')
					{
						parts.add(treeStr.substring(index, i));
						index = i - 1;
						break;
					}
				}
			}
		}
		return parts;
	}

	// 将括号表达式的list转化为一棵树,树中节点的angle和位置信息没有确定 (NP(NN(小篮球))(NN(联赛))(NN(启动))(NN(仪式)))
	public ArrayList<TreePanelNode> getTree2(ArrayList<String> list)
	{
		if (list.isEmpty())
			return null;
		ArrayList<TreePanelNode> treeLists = new ArrayList<TreePanelNode>();
		Stack<TreePanelNode> words = new Stack<TreePanelNode>();

		int count = 0;// 计算生成树中一共有多少节点

		int len = list.size();
		for (int i = 0; i < len; i++)
		{// 扫描list，
			if (list.get(i).equals("("))// 扫描到(
				continue;
			else
			{
				if (!(list.get(i).equals(")")))
				{// 扫描到word
					if (words.isEmpty())
					{// 扫描到第一个word，即根节点的value
						TreePanelNode root = new TreePanelNode();
						root.setRoot(root);
						root.setParent(null);
						root.setChildren(new Vector<TreePanelNode>());
						root.setValue(list.get(i));
						words.push(root);
						treeLists.add(root);
						count++;
					}
					else
					{// 扫描到后面的word
						TreePanelNode child = new TreePanelNode();
						child.setRoot(words.peek().getRoot());
						child.setParent(words.peek());
						words.peek().getChildren().add(child);
						child.setChildren(new Vector<TreePanelNode>());
						child.setValue(list.get(i));
						words.push(child);
						count++;
					}
				}
				else
				{// 扫描到 )
					if (!words.isEmpty())
					{
						words.pop();
					}
				}

			}
		}
		if (words.isEmpty() && count != 0)
			return treeLists;
		else
			return null;

	}

	/*
	 * 将括号表达式的list转化为一棵树,树中节点的angle和位置信息没有确定 排除括号表达式上面的句子 ,如：
	 * 中国篮球协会在北京市通州区张家湾镇中心小学举行了小篮球发展计划暨小篮球联赛启动仪式。
	 * (ROOT(IP(NP(NP(NR(中国)))(NP(NN(篮球))(NN(协会))))(VP(PP(P(在))(NP(NP(NR(北京市))(NR(
	 * 通州区))(NR(张家湾镇)))(NP(NN(中心))(NN(小学)))))(VP(VV(举行))(AS(了))(NP(NP(NN(小篮球))(NN(发展
	 * ))(NN(计划)))(CC(暨))(NP(NN(小篮球))(NN(联赛))(NN(启动))(NN(仪式))))(PU(。))))
	 * 小篮球规则适用对象为12岁及以下的少年儿童。
	 * (ROOT(IP(NP(NN(小篮球))(NN(规则))(NN(适用))(NN(对象)))(VP(VC(为)))(NP(DNP(QP(CD(12))(
	 * CLP(M(岁)))(CC(及))(NP(PN(以下))))(DEG(的)))(NP(NN(少年))(NN(儿童))))(PU(。))))
	 * 能够将描述括号表达式的句子去掉
	 */
	public ArrayList<TreePanelNode> getTree(ArrayList<String> list)
	{
		if (list.isEmpty())
			return null;
		ArrayList<TreePanelNode> treeLists = new ArrayList<TreePanelNode>();
		Stack<TreePanelNode> words = new Stack<TreePanelNode>();

		int count = 0;// 计算生成树中一共有多少节点

		int len = list.size();
		for (int i = 0; i < len; i++)
		{// 扫描list，
			if (list.get(i).equals("("))
			{// 扫描到(
				TreePanelNode left = new TreePanelNode();// 添加左括号是为了排除括号表达式左边的句子
				left.setValue("(");
				words.push(left);
			}

			else
			{
				if (!(list.get(i).equals(")")))
				{// 扫描到word
					if (words.isEmpty())
						continue;
					if (words.peek().getValue().equals("(") && count == 0)
					{// 扫描到第一个word，即根节点的value
						words.pop();// 将栈顶的（ 弹出
						TreePanelNode root = new TreePanelNode();
						root.setRoot(root);
						root.setParent(null);
						root.setChildren(new Vector<TreePanelNode>());
						root.setValue(list.get(i));
						words.push(root);
						treeLists.add(root);
						count++;
					}
					else if (words.peek().getValue().equals("("))
					{// 扫描到后面的word
						words.pop();// 将栈顶的（ 弹出
						TreePanelNode child = new TreePanelNode();
						child.setRoot(words.peek().getRoot());
						child.setParent(words.peek());
						words.peek().getChildren().add(child);
						child.setChildren(new Vector<TreePanelNode>());
						child.setValue(list.get(i));
						words.push(child);
						count++;
					}
				}
				else
				{// 扫描到 )
					if (!words.isEmpty())
					{
						words.pop();
					}
					else
						return null;
				}
			}
			if (words.isEmpty()) // 转化完成一棵树
				count = 0;

		}
		if (words.isEmpty() && treeLists != null)
			return treeLists;
		else
			return null;

	}

	/*
	 * 
	 * 
	 * 
	 * 将树转化为括号表达式
	 */
	// 将树转化为一个字符串数组，其中包括添加的用于换行和空格的字符串
	public LinkedList<String> changeIntoText()
	{

		LinkedList<String> tree = new LinkedList<String>();
		if (treeToText(this, tree) != -1)
		{// 表示不是空树
			tree.set(0, "(");
			return tree;
		}
		else
			return null;
	}

	// //将树转化为单行的括号表达式
	// public LinkedList<String> changeIntoList(){
	//
	// LinkedList<String> tree = new LinkedList<String>();
	// if(treeToList(this,tree) != -1)
	// return tree;
	// else
	// return null;
	// }

	// 将树的节点插入到list中，并返回根节点在list中的索引。包括换行符和空格，便于将list输出到文本中成为一个多行有格式的括号表达式。
	public int treeToText(TreePanelNode root, LinkedList<String> listOfTree)
	{// 该函数表示将root为根的树排列好在list中，并返回root节点在list中的索引，同时判断该根节点是否有儿子节点，
		// 如果该节点有儿子节点且儿子节点不是叶子节点就在list中root的'('前插入换行符和一定的空格组成的字符串。
		if (root == null)
		{
			return -1;
		}

		int indexOfFirstChild = -1;
		int indexOfRoot = -1;
		if (root.getChildren().size() != 0)
		{
			int count = 0;
			for (; count < root.getChildren().size();)
			{// 把所有以root的儿子为根节点的子树插入到listOfTree中去，并返回第一个儿子在listOfTree中的索引
				count++;
				if (count == 1)
				{
					indexOfFirstChild = treeToText(root.getChildren().get(count - 1), listOfTree);
				}
				else
				{
					treeToText(root.getChildren().get(count - 1), listOfTree);
				}
			}
			// 处理根节点
			// System.out.println("indexOfFirst====" + indexOfFirstChild);

			if (!isInsertString(root.getChildren().get(0)))
			{// 判断root的第一个孩子节点前面是否添加调整格式的字符串，if成立表示没有添加
				if (!isInsertString(root))
				{// 判断root节点前面是否用添加格式字符串。未添加
					listOfTree.add(indexOfFirstChild - 1, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 1, " (");// root的index和第一个孩子返回的index大小相同
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild;
				}
				else
				{// root节点前面要添加格式字符串
					listOfTree.add(indexOfFirstChild - 1, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 1, " (");// root的index和第一个孩子返回的index大小相同
					String format = "";
					for (int i = 0; i < root.height(); i++)
					{
						format = format + " ";
						format = format + " ";
					}
					if (root.height() != 0)
						format = format.substring(0, format.length() - 1);
					listOfTree.add(indexOfFirstChild - 1, "\r\n" + format);
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild + 1;
				}
			}
			else
			{
				// 即判断root节点前面是否用添加格式字符串
				if (!isInsertString(root))
				{// root节点前面不用添加格式字符串
					listOfTree.add(indexOfFirstChild - 2, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 2, " (");// root的index和第一个孩子返回的index大小相同
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild - 1;
				}
				else
				{// root节点前面要添加格式字符串
					listOfTree.add(indexOfFirstChild - 2, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 2, " (");// root的index和第一个孩子返回的index大小相同
					String format = "\r\n";
					for (int i = 0; i < root.height(); i++)
					{
						format = format + "  ";
					}
					if (root.height() != 0)
						format = format.substring(0, format.length() - 1);
					listOfTree.add(indexOfFirstChild - 2, format);
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild;
				}
			}

		}
		else
		{// 处理root是叶子节点，结束递归的条件
			if (root.equals(this.getRoot()))
			{// 即树上只有一个根节点
				listOfTree.add("(");
				listOfTree.add(root.getValue().toString());
				listOfTree.add(")");
				indexOfRoot = 1;
			}
			else
			{
				listOfTree.add(" ");// 把叶子节点外边的括号删除
				listOfTree.add(root.getValue().toString());
				indexOfFirstChild = listOfTree.size() - 1;
				// listOfTree.add(")");
				indexOfRoot = indexOfFirstChild;
			}

		}

		return indexOfRoot;
	}

	// 判断list中该节点之前是否要插入格式字符串
	private boolean isInsertString(TreePanelNode node)
	{
		if (node.equals(node.getRoot()))
			return false;
		if (node == null)
			return false;
		if (node.getChildren().isEmpty())
			return false;
		else if (!node.getChildren().get(0).getChildren().isEmpty())
			return true;
		else
		{
			for (TreePanelNode siblingBeforeNode : node.getParent().getChildren())
			{
				if (siblingBeforeNode.equals(node))
					break;
				if (isInsertString(siblingBeforeNode))
					return true;
			}
			return false;
		}
	}

	// 将树的节点插入到list中，并返回根节点在list中的索引。包括换行符和空格，便于将list输出到文本中成为一个多行有格式的括号表达式。
	public int treeToText2(TreePanelNode root, LinkedList<String> listOfTree)
	{// 该函数表示将root为根的树排列好在list中，并返回root节点在list中的索引，同时判断该根节点是否有儿子节点，
		// 如果该节点有儿子节点且儿子节点不是叶子节点就在list中root的'('前插入换行符和一定的空格组成的字符串。
		if (root == null)
			return -1;
		int indexOfFirstChild = -1;
		int indexOfRoot = -1;
		if (root.getChildren().size() != 0)
		{
			int count = 0;
			for (; count < root.getChildren().size();)
			{// 把所有以root的儿子为根节点的子树插入到listOfTree中去，并返回第一个儿子在listOfTree中的索引
				count++;
				if (count == 1)
				{
					indexOfFirstChild = treeToText(root.getChildren().get(count - 1), listOfTree);
				}
				else
				{
					treeToText(root.getChildren().get(count - 1), listOfTree);
				}
			}
			// 处理根节点
			// System.out.println("indexOfFirst===="+indexOfFirstChild);
			// 判断root的第一个孩子节点是否有儿子，即判断list中root的第一个孩子节点前是否添加了一个由换行符和空格组成的字符串
			if (root.getChildren().get(0).getChildren().size() == 0
					|| root.getChildren().get(0).getChildren().get(0).getChildren().size() == 0)
			{// if成立表示root的第一个孩子节点没有儿子，或者root的第一个孩子节点的孩子是叶子节点，root的第一个孩子节点前面没有添加调整格式的字符串
				// 判断该root节点的儿子节点是否是叶子节点，(最外层if已经判断出该root节点有儿子)，即判断root节点前面是否用添加格式字符串
				if (root.getChildren().get(0).getChildren().size() == 0)
				{// 是叶子节点，root节点前面不用添加格式字符串
					listOfTree.add(indexOfFirstChild - 1, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 1, "(");// root的index和第一个孩子返回的index大小相同
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild;
				}
				else
				{// 不是叶子节点，root节点前面要添加格式字符串
					listOfTree.add(indexOfFirstChild - 1, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 1, "(");// root的index和第一个孩子返回的index大小相同
					String format = "";
					for (int i = 0; i < root.height(); i++)
					{
						format = format + " ";
						format = format + " ";
					}
					listOfTree.add(indexOfFirstChild - 1, "\r\n" + format);
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild + 1;
				}
			}
			else
			{// root的第一个孩子节点前面添加了调整格式的字符串
				// 判断该root节点的儿子节点是否是叶子节点，(最外层if已经判断出该root节点有儿子)，即判断root节点前面是否用添加格式字符串
				if (root.getChildren().get(0).getChildren().size() == 0)
				{// 是叶子节点，root节点前面不用添加格式字符串
					listOfTree.add(indexOfFirstChild - 2, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 2, "(");// root的index和第一个孩子返回的index大小相同
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild - 1;
				}
				else
				{// 不是叶子节点,root节点前面要添加格式字符串
					listOfTree.add(indexOfFirstChild - 2, root.getValue().toString());
					listOfTree.add(indexOfFirstChild - 2, "(");// root的index和第一个孩子返回的index大小相同
					String format = "";
					for (int i = 0; i < root.height(); i++)
					{
						format = format + " ";
						format = format + " ";
					}
					listOfTree.add(indexOfFirstChild - 2, "\r\n" + format);
					listOfTree.add(")");
					indexOfRoot = indexOfFirstChild;
				}
			}

		}
		else
		{// 处理root是叶子节点，结束递归的条件
			listOfTree.add("(");
			listOfTree.add(root.getValue().toString());
			indexOfFirstChild = listOfTree.size() - 1;
			listOfTree.add(")");
			indexOfRoot = indexOfFirstChild;
		}

		return indexOfRoot;
	}

	// 将树的节点插入到list中，并返回根节点在list中的索引，没有换行符和多余空格
	public int treeToList(TreePanelNode root, LinkedList<String> listOfTree)
	{// 将root为根的树排列好在list中，并返回root节点在list中的索引
		if (root == null)
			return -1;
		int indexOfFirst = -1;
		if (root.getChildren().size() != 0)
		{
			int count = 0;
			for (; count < root.getChildren().size();)
			{// 把所有以root的儿子为根节点的子树插入到listOfTree中去，并返回第一个儿子在listOfTree中的索引
				count++;
				if (count == 1)
				{
					indexOfFirst = treeToList(root.getChildren().get(count - 1), listOfTree);
				}
				else
				{
					treeToList(root.getChildren().get(count - 1), listOfTree);
				}
			}
			// 处理根节点
			// System.out.println("indexOfFirst===="+indexOfFirst);
			listOfTree.add(indexOfFirst - 1, root.getValue().toString());
			listOfTree.add(indexOfFirst - 1, "(");// root的index和第一个孩子反悔的index大小相同
			listOfTree.add(")");
		}
		else
		{// 处理叶子节点，结束递归的条件
			listOfTree.add("(");
			listOfTree.add(root.getValue().toString());
			indexOfFirst = listOfTree.size() - 1;
			listOfTree.add(")");
		}

		return indexOfFirst;
	}

	public void addChild(TreePanelNode n)
	{
		addChild(children.size(), n);
	}

	public void addChild(int index, TreePanelNode n)
	{
		children.add(index, n);
		n.parent = this;
	}

	public void removeChild(TreePanelNode n)
	{
		children.remove(n);
	}

	public void removeChild(int index)
	{
		children.remove(index);
	}

	public void setLocation(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Point getLocation()
	{
		return new Point(x, y);
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public Rectangle getBounds()
	{
		return new Rectangle(x, y, width, height);
	}

	public Vector<TreePanelNode> getChildren()
	{
		return children;
	}

	public void setChildren(Vector<TreePanelNode> children)
	{
		if (children != null)
			this.children = children;
		else
			this.children = new Vector<TreePanelNode>();
	}

	public TreePanelNode getParent()
	{
		return parent;
	}

	public void setParent(TreePanelNode parent)
	{
		this.parent = parent;
	}

	public TreePanelNode getRoot()
	{
		return root;
	}

	public void setRoot(TreePanelNode root)
	{
		this.root = root;
	}

	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public double getAngle()
	{
		return angle;
	}

	public void setAngle(double angle)
	{
		this.angle = angle;
	}

	// 测试输出树的基本值
	public void print()
	{
		// 测试
		int count = -1;
		for (TreePanelNode theNode : allNodes(this.getRoot()))
		{
			count++;
			System.out.println("count:" + count + theNode.getValue() + "的x,y为：" + theNode.getX() + "," + theNode.getY()
					+ "角度是" + theNode.getAngle());
			if (!theNode.getChildren().isEmpty())
				System.out.println("firstChlild:" + theNode.getChildren().get(0).getValue().toString());
			if (theNode.getParent() != null)
				System.out.println("parent" + theNode.getParent().getValue().toString());
		}
	}

}
