/* Generated by AN DISI Unibo */ 
package it.unibo.mindrobot;
import it.unibo.qactors.PlanRepeat;
import it.unibo.qactors.QActorContext;
import it.unibo.qactors.StateExecMessage;
import it.unibo.qactors.QActorUtils;
import it.unibo.is.interfaces.IOutputEnvView;
import it.unibo.qactors.action.AsynchActionResult;
import it.unibo.qactors.action.IActorAction;
import it.unibo.qactors.action.IActorAction.ActionExecMode;
import it.unibo.qactors.action.IMsgQueue;
import it.unibo.qactors.akka.QActor;
import it.unibo.qactors.StateFun;
import java.util.Stack;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import alice.tuprolog.Struct;
import alice.tuprolog.Term;
import it.unibo.qactors.action.ActorTimedAction;
public abstract class AbstractMindrobot extends QActor { 
	protected AsynchActionResult aar = null;
	protected boolean actionResult = true;
	protected alice.tuprolog.SolveInfo sol;
	protected String planFilePath    = null;
	protected String terminationEvId = "default";
	protected String parg="";
	protected boolean bres=false;
	protected IActorAction action;
	 
	
		protected static IOutputEnvView setTheEnv(IOutputEnvView outEnvView ){
			return outEnvView;
		}
		public AbstractMindrobot(String actorId, QActorContext myCtx, IOutputEnvView outEnvView )  throws Exception{
			super(actorId, myCtx,  
			"./srcMore/it/unibo/mindrobot/WorldTheory.pl",
			setTheEnv( outEnvView )  , "init");
			this.planFilePath = "./srcMore/it/unibo/mindrobot/plans.txt";
	  	}
		@Override
		protected void doJob() throws Exception {
			String name  = getName().replace("_ctrl", "");
			mysupport = (IMsgQueue) QActorUtils.getQActor( name ); 
			initStateTable(); 
	 		initSensorSystem();
	 		history.push(stateTab.get( "init" ));
	  	 	autoSendStateExecMsg();
	  		//QActorContext.terminateQActorSystem(this);//todo
		} 	
		/* 
		* ------------------------------------------------------------
		* PLANS
		* ------------------------------------------------------------
		*/    
	    //genAkkaMshHandleStructure
	    protected void initStateTable(){  	
	    	stateTab.put("handleToutBuiltIn",handleToutBuiltIn);
	    	stateTab.put("init",init);
	    	stateTab.put("afterInit",afterInit);
	    	stateTab.put("waitPlan",waitPlan);
	    	stateTab.put("handleEvent",handleEvent);
	    	stateTab.put("handleChange",handleChange);
	    	stateTab.put("handleSonarChange",handleSonarChange);
	    	stateTab.put("handleMsg",handleMsg);
	    }
	    StateFun handleToutBuiltIn = () -> {	
	    	try{	
	    		PlanRepeat pr = PlanRepeat.setUp("handleTout",-1);
	    		String myselfName = "handleToutBuiltIn";  
	    		println( "mindrobot tout : stops");  
	    		repeatPlanNoTransition(pr,myselfName,"application_"+myselfName,false,false);
	    	}catch(Exception e_handleToutBuiltIn){  
	    		println( getName() + " plan=handleToutBuiltIn WARNING:" + e_handleToutBuiltIn.getMessage() );
	    		QActorContext.terminateQActorSystem(this); 
	    	}
	    };//handleToutBuiltIn
	    
	    StateFun init = () -> {	
	    try{	
	     PlanRepeat pr = PlanRepeat.setUp("init",-1);
	    	String myselfName = "init";  
	    	parg = "consult(\"./resourceModel.pl\")";
	    	//QActorUtils.solveGoal(myself,parg,pengine );  //sets currentActionResult		
	    	solveGoal( parg ); //sept2017
	    	temporaryStr = "\"Mind robot ready\"";
	    	println( temporaryStr );  
	     connectToMqttServer("ws://localhost:1884");
	    	//switchTo afterInit
	        switchToPlanAsNextState(pr, myselfName, "mindrobot_"+myselfName, 
	              "afterInit",false, false, null); 
	    }catch(Exception e_init){  
	    	 println( getName() + " plan=init WARNING:" + e_init.getMessage() );
	    	 QActorContext.terminateQActorSystem(this); 
	    }
	    };//init
	    
	    StateFun afterInit = () -> {	
	    try{	
	     PlanRepeat pr = PlanRepeat.setUp("afterInit",-1);
	    	String myselfName = "afterInit";  
	    	it.unibo.utils.customDate.getHoursRM( myself  );
	    	parg = "getModelItem(sensor,clock,clock1,R)";
	    	//QActorUtils.solveGoal(myself,parg,pengine );  //sets currentActionResult		
	    	solveGoal( parg ); //sept2017
	    	if( (guardVars = QActorUtils.evalTheGuard(this, " ??goalResult(getModelItem(sensor,clock,clock1,R))" )) != null ){
	    	//PublisEventhMove
	    	parg = "resourceChangeEvent(sensor,clock1,R)";
	    	parg = QActorUtils.substituteVars(guardVars,parg);
	    	sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    	}
	    	//switchTo waitPlan
	        switchToPlanAsNextState(pr, myselfName, "mindrobot_"+myselfName, 
	              "waitPlan",false, false, null); 
	    }catch(Exception e_afterInit){  
	    	 println( getName() + " plan=afterInit WARNING:" + e_afterInit.getMessage() );
	    	 QActorContext.terminateQActorSystem(this); 
	    }
	    };//afterInit
	    
	    StateFun waitPlan = () -> {	
	    try{	
	     PlanRepeat pr = PlanRepeat.setUp(getName()+"_waitPlan",0);
	     pr.incNumIter(); 	
	    	String myselfName = "waitPlan";  
	    	//bbb
	     msgTransition( pr,myselfName,"mindrobot_"+myselfName,false,
	          new StateFun[]{stateTab.get("handleEvent"), stateTab.get("handleChange"), stateTab.get("handleSonarChange"), stateTab.get("handleSonarChange"), stateTab.get("handleMsg") }, 
	          new String[]{"true","E","resourceChangeEvent", "true","E","resourceChange", "true","E","sonar", "true","E","sonarDetect", "true","M","moveRobot" },
	          3600000, "handleToutBuiltIn" );//msgTransition
	    }catch(Exception e_waitPlan){  
	    	 println( getName() + " plan=waitPlan WARNING:" + e_waitPlan.getMessage() );
	    	 QActorContext.terminateQActorSystem(this); 
	    }
	    };//waitPlan
	    
	    StateFun handleEvent = () -> {	
	    try{	
	     PlanRepeat pr = PlanRepeat.setUp("handleEvent",-1);
	    	String myselfName = "handleEvent";  
	    	printCurrentEvent(false);
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("resourceChangeEvent(sensor,cityTemperature,V)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("resourceChangeEvent") && 
	    		pengine.unify(curT, Term.createTerm("resourceChangeEvent(TYPE,NAME,VALUE)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			String parg="changeModelItem(temperature,cityTemperature,V)";
	    			/* PHead */
	    			parg =  updateVars( Term.createTerm("resourceChangeEvent(TYPE,NAME,VALUE)"), 
	    			                    Term.createTerm("resourceChangeEvent(sensor,cityTemperature,V)"), 
	    				    		  	Term.createTerm(currentEvent.getMsg()), parg);
	    				if( parg != null ) {
	    				    aar = QActorUtils.solveGoal(this,myCtx,pengine,parg,"",outEnvView,86400000);
	    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
	    					if( aar.getInterrupted() ){
	    						curPlanInExec   = "handleEvent";
	    						if( aar.getTimeRemained() <= 0 ) addRule("tout(demo,"+getName()+")");
	    						if( ! aar.getGoon() ) return ;
	    					} 			
	    					if( aar.getResult().equals("failure")){
	    						if( ! aar.getGoon() ) return ;
	    					}else if( ! aar.getGoon() ) return ;
	    				}
	    	}
	    	repeatPlanNoTransition(pr,myselfName,"mindrobot_"+myselfName,false,true);
	    }catch(Exception e_handleEvent){  
	    	 println( getName() + " plan=handleEvent WARNING:" + e_handleEvent.getMessage() );
	    	 QActorContext.terminateQActorSystem(this); 
	    }
	    };//handleEvent
	    
	    StateFun handleChange = () -> {	
	    try{	
	     PlanRepeat pr = PlanRepeat.setUp("handleChange",-1);
	    	String myselfName = "handleChange";  
	    	printCurrentEvent(false);
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("resourceChange(sensor,CATEG,NAME,off)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("resourceChange") && 
	    		pengine.unify(curT, Term.createTerm("resourceChange(TYPE,CATEG,NAME,VALUE)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			//println("WARNING: variable substitution not yet fully implemented " ); 
	    			{//actionseq
	    			//PublishMsgMove
	    			parg = "usercmd(robotgui(h(low)))";
	    			sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "virtualrobotexecutor", parg );
	    			//PublishMsgMove
	    			parg = "usercmd(robotgui(h(low)))";
	    			sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "realrobotexecutor", parg );
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(executor,soffritti,off)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(executor,fuffolo,off)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			temporaryStr = "\"Mqtt emesso\"";
	    			println( temporaryStr );  
	    			parg = "changeModelItem(leds,NAME,off)";
	    			//QActorUtils.solveGoal(myself,parg,pengine );  //sets currentActionResult		
	    			solveGoal( parg ); //sept2017
	    			};//actionseq
	    	}
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("resourceChange(sensor,CATEG,NAME,on)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("resourceChange") && 
	    		pengine.unify(curT, Term.createTerm("resourceChange(TYPE,CATEG,NAME,VALUE)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			//println("WARNING: variable substitution not yet fully implemented " ); 
	    			{//actionseq
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(executor,soffritti,on)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(executor,fuffolo,on)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			};//actionseq
	    	}
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("resourceChange(actuator,leds,NAME,off)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("resourceChange") && 
	    		pengine.unify(curT, Term.createTerm("resourceChange(TYPE,CATEG,NAME,VALUE)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			//println("WARNING: variable substitution not yet fully implemented " ); 
	    			{//actionseq
	    			it.unibo.utils.clientRest.sendPutBlink( myself ,"false", "#00ff00", "1"  );
	    			it.unibo.utils.clientRest.sendPutBlink( myself ,"false", "#00ff00", "2"  );
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(actuator,ledHue,off)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(actuator,ledReal,off)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			};//actionseq
	    	}
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("resourceChange(actuator,leds,NAME,on)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("resourceChange") && 
	    		pengine.unify(curT, Term.createTerm("resourceChange(TYPE,CATEG,NAME,VALUE)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			//println("WARNING: variable substitution not yet fully implemented " ); 
	    			{//actionseq
	    			it.unibo.utils.clientRest.sendPutBlink( myself ,"true", "#00ff00", "1"  );
	    			it.unibo.utils.clientRest.sendPutBlink( myself ,"true", "#00ff00", "2"  );
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(actuator,ledHue,on)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			//PublisEventhMove
	    			parg = "resourceChangeEvent(actuator,ledReal,on)";
	    			sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    			};//actionseq
	    	}
	    	repeatPlanNoTransition(pr,myselfName,"mindrobot_"+myselfName,false,true);
	    }catch(Exception e_handleChange){  
	    	 println( getName() + " plan=handleChange WARNING:" + e_handleChange.getMessage() );
	    	 QActorContext.terminateQActorSystem(this); 
	    }
	    };//handleChange
	    
	    StateFun handleSonarChange = () -> {	
	    try{	
	     PlanRepeat pr = PlanRepeat.setUp("handleSonarChange",-1);
	    	String myselfName = "handleSonarChange";  
	    	printCurrentEvent(false);
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("sonar(sonar1,soffritti,DISTANCE)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("sonar") && 
	    		pengine.unify(curT, Term.createTerm("sonar(NAME,ROBOT,DISTANCE)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			String parg="changeModelItem(sonarVirtual,sonar1,DISTANCE)";
	    			/* PHead */
	    			parg =  updateVars( Term.createTerm("sonar(NAME,ROBOT,DISTANCE)"), 
	    			                    Term.createTerm("sonar(sonar1,soffritti,DISTANCE)"), 
	    				    		  	Term.createTerm(currentEvent.getMsg()), parg);
	    				if( parg != null ) {
	    				    aar = QActorUtils.solveGoal(this,myCtx,pengine,parg,"",outEnvView,86400000);
	    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
	    					if( aar.getInterrupted() ){
	    						curPlanInExec   = "handleSonarChange";
	    						if( aar.getTimeRemained() <= 0 ) addRule("tout(demo,"+getName()+")");
	    						if( ! aar.getGoon() ) return ;
	    					} 			
	    					if( aar.getResult().equals("failure")){
	    						if( ! aar.getGoon() ) return ;
	    					}else if( ! aar.getGoon() ) return ;
	    				}
	    	}
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("sonar(sonar2,soffritti,DISTANCE)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("sonar") && 
	    		pengine.unify(curT, Term.createTerm("sonar(NAME,ROBOT,DISTANCE)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			String parg="changeModelItem(sonarVirtual,sonar2,DISTANCE)";
	    			/* PHead */
	    			parg =  updateVars( Term.createTerm("sonar(NAME,ROBOT,DISTANCE)"), 
	    			                    Term.createTerm("sonar(sonar2,soffritti,DISTANCE)"), 
	    				    		  	Term.createTerm(currentEvent.getMsg()), parg);
	    				if( parg != null ) {
	    				    aar = QActorUtils.solveGoal(this,myCtx,pengine,parg,"",outEnvView,86400000);
	    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
	    					if( aar.getInterrupted() ){
	    						curPlanInExec   = "handleSonarChange";
	    						if( aar.getTimeRemained() <= 0 ) addRule("tout(demo,"+getName()+")");
	    						if( ! aar.getGoon() ) return ;
	    					} 			
	    					if( aar.getResult().equals("failure")){
	    						if( ! aar.getGoon() ) return ;
	    					}else if( ! aar.getGoon() ) return ;
	    				}
	    	}
	    	//onEvent 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("sonarDetect(TARGET,soffritti)");
	    	if( currentEvent != null && currentEvent.getEventId().equals("sonarDetect") && 
	    		pengine.unify(curT, Term.createTerm("sonarDetect(TARGET,ROBOT)")) && 
	    		pengine.unify(curT, Term.createTerm( currentEvent.getMsg() ) )){ 
	    			String parg="changeModelItem(sonarRobot,sonarVirtual,TARGET)";
	    			/* PHead */
	    			parg =  updateVars( Term.createTerm("sonarDetect(TARGET,ROBOT)"), 
	    			                    Term.createTerm("sonarDetect(TARGET,soffritti)"), 
	    				    		  	Term.createTerm(currentEvent.getMsg()), parg);
	    				if( parg != null ) {
	    				    aar = QActorUtils.solveGoal(this,myCtx,pengine,parg,"",outEnvView,86400000);
	    					//println(getName() + " plan " + curPlanInExec  +  " interrupted=" + aar.getInterrupted() + " action goon="+aar.getGoon());
	    					if( aar.getInterrupted() ){
	    						curPlanInExec   = "handleSonarChange";
	    						if( aar.getTimeRemained() <= 0 ) addRule("tout(demo,"+getName()+")");
	    						if( ! aar.getGoon() ) return ;
	    					} 			
	    					if( aar.getResult().equals("failure")){
	    						if( ! aar.getGoon() ) return ;
	    					}else if( ! aar.getGoon() ) return ;
	    				}
	    	}
	    	repeatPlanNoTransition(pr,myselfName,"mindrobot_"+myselfName,false,true);
	    }catch(Exception e_handleSonarChange){  
	    	 println( getName() + " plan=handleSonarChange WARNING:" + e_handleSonarChange.getMessage() );
	    	 QActorContext.terminateQActorSystem(this); 
	    }
	    };//handleSonarChange
	    
	    StateFun handleMsg = () -> {	
	    try{	
	     PlanRepeat pr = PlanRepeat.setUp("handleMsg",-1);
	    	String myselfName = "handleMsg";  
	    	it.unibo.utils.customDate.getHoursRM( myself  );
	    	parg = "getModelItem(sensor,clock,clock1,R)";
	    	//QActorUtils.solveGoal(myself,parg,pengine );  //sets currentActionResult		
	    	solveGoal( parg ); //sept2017
	    	if( (guardVars = QActorUtils.evalTheGuard(this, " ??goalResult(getModelItem(sensor,clock,clock1,R))" )) != null ){
	    	//PublisEventhMove
	    	parg = "resourceChangeEvent(sensor,clock1,R)";
	    	parg = QActorUtils.substituteVars(guardVars,parg);
	    	sendMsgMqtt(  "unibo/qasys", "resourceChangeEvent", "none", parg );
	    	}
	    	printCurrentMessage(false);
	    	//onMsg 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("usercmd(robotgui(w(X)))");
	    	if( currentMessage != null && currentMessage.msgId().equals("moveRobot") && 
	    		pengine.unify(curT, Term.createTerm("usercmd(CMD)")) && 
	    		pengine.unify(curT, Term.createTerm( currentMessage.msgContent() ) )){ 
	    		//println("WARNING: variable substitution not yet fully implemented " ); 
	    		{//actionseq
	    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?model(type(executor,X),name(Y),value(true))" )) != null ){
	    		{//actionseq
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(w(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "virtualrobotexecutor", parg );
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(w(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "realrobotexecutor", parg );
	    		parg = "changeModelItem(leds,NAME,on)";
	    		//QActorUtils.solveGoal(myself,parg,pengine );  //sets currentActionResult		
	    		solveGoal( parg ); //sept2017
	    		};//actionseq
	    		}
	    		else{ temporaryStr = "\"Too hot to work or out of time\"";
	    		println( temporaryStr );  
	    		}};//actionseq
	    	}
	    	//onMsg 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("usercmd(robotgui(s(X)))");
	    	if( currentMessage != null && currentMessage.msgId().equals("moveRobot") && 
	    		pengine.unify(curT, Term.createTerm("usercmd(CMD)")) && 
	    		pengine.unify(curT, Term.createTerm( currentMessage.msgContent() ) )){ 
	    		//println("WARNING: variable substitution not yet fully implemented " ); 
	    		{//actionseq
	    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?model(type(executor,X),name(Y),value(true))" )) != null ){
	    		{//actionseq
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(s(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "virtualrobotexecutor", parg );
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(s(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "realrobotexecutor", parg );
	    		parg = "changeModelItem(leds,NAME,on)";
	    		//QActorUtils.solveGoal(myself,parg,pengine );  //sets currentActionResult		
	    		solveGoal( parg ); //sept2017
	    		};//actionseq
	    		}
	    		else{ temporaryStr = "\"Too hot to work or out of time\"";
	    		println( temporaryStr );  
	    		}};//actionseq
	    	}
	    	//onMsg 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("usercmd(robotgui(a(X)))");
	    	if( currentMessage != null && currentMessage.msgId().equals("moveRobot") && 
	    		pengine.unify(curT, Term.createTerm("usercmd(CMD)")) && 
	    		pengine.unify(curT, Term.createTerm( currentMessage.msgContent() ) )){ 
	    		//println("WARNING: variable substitution not yet fully implemented " ); 
	    		{//actionseq
	    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?model(type(executor,X),name(Y),value(true))" )) != null ){
	    		{//actionseq
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(a(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "virtualrobotexecutor", parg );
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(a(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "realrobotexecutor", parg );
	    		};//actionseq
	    		}
	    		else{ temporaryStr = "\"Too hot to work or out of time\"";
	    		println( temporaryStr );  
	    		}};//actionseq
	    	}
	    	//onMsg 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("usercmd(robotgui(d(X)))");
	    	if( currentMessage != null && currentMessage.msgId().equals("moveRobot") && 
	    		pengine.unify(curT, Term.createTerm("usercmd(CMD)")) && 
	    		pengine.unify(curT, Term.createTerm( currentMessage.msgContent() ) )){ 
	    		//println("WARNING: variable substitution not yet fully implemented " ); 
	    		{//actionseq
	    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?model(type(executor,X),name(Y),value(true))" )) != null ){
	    		{//actionseq
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(d(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "virtualrobotexecutor", parg );
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(d(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "realrobotexecutor", parg );
	    		};//actionseq
	    		}
	    		else{ temporaryStr = "\"Too hot to work or out of time\"";
	    		println( temporaryStr );  
	    		}};//actionseq
	    	}
	    	//onMsg 
	    	setCurrentMsgFromStore(); 
	    	curT = Term.createTerm("usercmd(robotgui(h(X)))");
	    	if( currentMessage != null && currentMessage.msgId().equals("moveRobot") && 
	    		pengine.unify(curT, Term.createTerm("usercmd(CMD)")) && 
	    		pengine.unify(curT, Term.createTerm( currentMessage.msgContent() ) )){ 
	    		//println("WARNING: variable substitution not yet fully implemented " ); 
	    		{//actionseq
	    		if( (guardVars = QActorUtils.evalTheGuard(this, " !?model(type(executor,X),name(Y),value(true))" )) != null ){
	    		{//actionseq
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(h(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "virtualrobotexecutor", parg );
	    		//PublishMsgMove
	    		parg = "usercmd(robotgui(h(low)))";
	    		sendMsgMqtt(  "unibo/qasys", "execMoveRobot", "realrobotexecutor", parg );
	    		parg = "changeModelItem(leds,NAME,off)";
	    		//QActorUtils.solveGoal(myself,parg,pengine );  //sets currentActionResult		
	    		solveGoal( parg ); //sept2017
	    		};//actionseq
	    		}
	    		else{ temporaryStr = "\"Too hot to work or out of time\"";
	    		println( temporaryStr );  
	    		}};//actionseq
	    	}
	    	repeatPlanNoTransition(pr,myselfName,"mindrobot_"+myselfName,false,true);
	    }catch(Exception e_handleMsg){  
	    	 println( getName() + " plan=handleMsg WARNING:" + e_handleMsg.getMessage() );
	    	 QActorContext.terminateQActorSystem(this); 
	    }
	    };//handleMsg
	    
	    protected void initSensorSystem(){
	    	//doing nothing in a QActor
	    }
	
	}
