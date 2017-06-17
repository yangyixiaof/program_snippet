package cn.yyx.research.program.ir;

public class IRMeta {
	
	public static final String MethodInvocation = "@MethodInvocation_";
	// public static final String User_Defined_Function = "@UserDefine";
	public static final String If = "@If";
	public static final String ConditionExpression = "@Cond_Exp";
	// public static final String IfThen = "@IfThen";
	// public static final String IfElse = "@IfElse";
	public static final String While = "@While";
	public static final String DoWhile = "@Do_While";
	public static final String For = "@For";
	public static final String For_Initial = "@For_Ini";
	public static final String For_Judge = "@For_Jdg";
	public static final String For_Update = "@For_Upd";
	public static final String EnhancedFor = "@ForEach";
	public static final String Break = "@Break";
	public static final String Continue = "@Continue";
	public static final String VariabledDeclare = "@VariabledDeclare";
	public static final String Synchronized = "@Synchronized";
	public static final String Return = "@Return";
	public static final String LeftHandAssign = "@LeftAssign";
	// public static final String RightHandAssign = "@RightHandAssign";
	public static final String Switch = "@Switch";
	public static final String Switch_Case_Default = "@SwitchCaseDefault";
	public static final String Switch_Case_Cause = "@SwitchCase_";
	public static final String Switch_Case_Relation = "@SwitchRelate_";
	public static final String Prefix = "@Prefix_";
	public static final String Postfix = "@Postfix_";
	public static final String InstanceOfType = "@InstanceOfType";
	public static final String InstanceOfExpression = "@InstanceOfExpression";
	public static final String InfixLeftExpression = "@InfixLeft_";
	public static final String InfixRightExpression = "@InfixRight_";
	public static final String CatchClause = "@CatchClause";
	public static final String CastType = "@CastType";
	public static final String CastExpression = "@CatchExpression";
	public static final String Array = "@Array";
	public static final String ArrayIndex = "@ArrayIndex";
	public static final String ArrayCreation = "@ArrayCreation";
	public static final String ArrayCreationIndex = "@ArrayCreationIndex";
	public static final String MethodReference = "@MethodRef_";
	public static final String FieldAccess = "@FieldAccess_";
	public static final String QualifiedName = "@QualifiedName_";
	
	public static final String VirtualMethodReturn = "@VirtualMethodReturn";
	public static final String VirtualSentinel = "@Sentinel_";
	
	public static final String BranchOver = "@BranchOver";
	public static final String VirtualBranch = "@VirtualBranch";
	
	public static final String ControlBranchJudge = "@VControlBranchJudge";
	public static final String ControlBranch = "@ControlBranch";
	public static final String ControlBranchOver = "@ControlBranchOver";
	
}
