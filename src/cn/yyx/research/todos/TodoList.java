package cn.yyx.research.todos;

public class TodoList {
	
	// Solved. source methods should be as one, not distributed in different element lists.
	// Solved. direct assignment should give a node to redirect to other list. Just add a Self connection to other list.
	// Solved. method return should be modeled as a distinct element, add Self connection to the method.
	// Solved. Construction method need to be reconsidered method and its class should be a map.
	// Solved. for common instructions, IJavaElement Self connection sill needs to be added.
	
	// Solved. remember to add virtual branch to every node in only one branch£¬ such as if(){} without else branch.
	// Solved. lambda expressions are needed to be treated as IMethod.
	// Solved. remember to check whether lambda expression method implementations could be searched.
	// Solved. method declaration remember to check parameter list, add null if.
	
	// Solved. variable count and two/three further steps' environment are no longer needed.
	// Solved. SkipSelfTask should add the information of IJavaElement if there are no source connections.
	// Solved. wrong when handling invoking constructors and super-methods in full traces.
	// Solved. if constructor is null, how to invoke field IRCode, IRCodeForOneFiled I mean.
	// Solved. do not traverse from root methods (exclude constructors) which contains no statements.
	
	// Solved. how to recognize the global relationship, eclipse jdt offers? append to tails of irs of that ije.
	// TODO how to recognize relationships including code snippet not on the execution path from main root?
	
	// Comment: the below two are the same.
	// Solved. the total IJavaElement temp_statement_expression_environment_set and temp_statement_environment_set need to be changed to Stack, former stack element gain all ijes of later stack element. do it in HandleIJavaElement.
	// Solved. designs such as temp_statement_expression_environment_set do have problems.
	
	// Solved. what is op:nothing.????? This has been changed to @VirtualBranch.
	// Solved. full trace branch codes are not right.
	
	// Solved. handle out control nodes related to return. Solved in BranchControlForOneIRCode's Pop and FullTrace's Node Creation.
	
	// Solved. some structures to show all_eles have been returned must be introduced which means all_eles have involved in that branch.
	// Solved. do not consider this case because this is the compilation error. what if some nodes are not connected, due to unlink of IRForOneReturn nodes.
	
	// these two are almost same questions.
	// TODO now branch_control has become independent systems, skip-self-tasks need to consider these special nodes.
	// TODO full_trace generation has ignored branch_control nodes but now the situation has changed. branch_control nodes should be taken into consideration.
	
	// Solved. ElementBranchInfo needs to be re-designed to Branch-Element-WhetherHasSet classical model.
	
}
