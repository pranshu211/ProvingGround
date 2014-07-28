package src.main.scala
import provingground.HoTT._
import provingground.InductiveTypes._

object HoTTExperiment {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  
  val A = "A" ::__                                //> A  : provingground.HoTT.Typ[provingground.HoTT.Term] with provingground.HoTT
                                                  //| .Subs[provingground.HoTT.Typ[provingground.HoTT.Term]] = A
  val a = "a" :: A                                //> a  : provingground.HoTT.Term with provingground.HoTT.Subs[provingground.HoTT
                                                  //| .Term] = (a : A)
  lambda(a)(a)                                    //> res0: provingground.HoTT.FuncTerm[provingground.HoTT.Term with provingground
                                                  //| .HoTT.Subs[provingground.HoTT.Term],provingground.HoTT.Term with provinggrou
                                                  //| nd.HoTT.Subs[provingground.HoTT.Term]] = ((a : A)⟼(a : A)
  TermOps(a) :-> a                                //> res1: provingground.HoTT.FuncTerm[provingground.HoTT.Term,provingground.HoTT
                                                  //| .Term with provingground.HoTT.Subs[provingground.HoTT.Term]] = ((a : A)⟼(a
                                                  //|  : A)
  def IdFn(A : Typ[Term]) = {
  	val a = "a" :: A
  	lambda(a)(a)
  }                                               //> IdFn: (A: provingground.HoTT.Typ[provingground.HoTT.Term])provingground.HoTT
                                                  //| .FuncTerm[provingground.HoTT.Term with provingground.HoTT.Subs[provingground
                                                  //| .HoTT.Term],provingground.HoTT.Term with provingground.HoTT.Subs[provinggrou
                                                  //| nd.HoTT.Term]]

	val Id = {
		val A = "A" :: __
		lambda(A)({
			val a = "a" :: A
			lambda(a)(a)}
		) }                               //> Id  : provingground.HoTT.FuncTerm[provingground.HoTT.Typ[provingground.HoTT.
                                                  //| Term] with provingground.HoTT.Subs[provingground.HoTT.Typ[provingground.HoTT
                                                  //| .Term]],provingground.HoTT.FuncTerm[provingground.HoTT.Term with provinggrou
                                                  //| nd.HoTT.Subs[provingground.HoTT.Term],provingground.HoTT.Term with provinggr
                                                  //| ound.HoTT.Subs[provingground.HoTT.Term]]] = (A⟼((a : A)⟼(a : A)
  
  Id(A)                                           //> res2: provingground.HoTT.FuncTerm[provingground.HoTT.Term with provingground
                                                  //| .HoTT.Subs[provingground.HoTT.Term],provingground.HoTT.Term with provinggrou
                                                  //| nd.HoTT.Subs[provingground.HoTT.Term]] = ((a : A)⟼(a : A)
  
  
  Id(A)(a)                                        //> res3: provingground.HoTT.Term with provingground.HoTT.Subs[provingground.HoT
                                                  //| T.Term] = (a : A)
   
  Id.typ                                          //> res4: provingground.HoTT.Typ[provingground.HoTT.Term] = Pi((A⟼(A⟶A))
	val MPall = {
		val A = "A" :: __
		val B = "B" :: __
		lambda(A)(
			lambda(B)({
			val a = "a" :: A
			val ab = "a->b" :: (A ->: B)
			lambda(a)(
				lambda(ab)(
					ab(a)
					))
			}))
		}                                 //> MPall  : provingground.HoTT.FuncTerm[provingground.HoTT.Typ[provingground.Ho
                                                  //| TT.Term] with provingground.HoTT.Subs[provingground.HoTT.Typ[provingground.H
                                                  //| oTT.Term]],provingground.HoTT.FuncTerm[provingground.HoTT.Typ[provingground.
                                                  //| HoTT.Term] with provingground.HoTT.Subs[provingground.HoTT.Typ[provingground
                                                  //| .HoTT.Term]],provingground.HoTT.FuncTerm[provingground.HoTT.Term with provin
                                                  //| gground.HoTT.Subs[provingground.HoTT.Term],provingground.HoTT.FuncTerm[provi
                                                  //| ngground.HoTT.FuncTerm[provingground.HoTT.Term,provingground.HoTT.Term] with
                                                  //|  provingground.HoTT.Subs[provingground.HoTT.FuncTerm[provingground.HoTT.Term
                                                  //| ,provingground.HoTT.Term]],provingground.HoTT.Term]]]] = (A⟼(B⟼((a : A)�628 ��((a->b : (A⟶B))⟼((a->b : (A⟶B))((a : A)) : B)
  MPall.typ                                       //> res5: provingground.HoTT.Typ[provingground.HoTT.Term] = Pi((A⟼Pi((B⟼(A�
                                                  //| �((A⟶B)⟶B))))
	val MP = {
		val A = "A" :: __
		val B = "B" :: __
			val a = "a" :: A
			val ab = "a->b" :: (A ->: B)
			lambda(a)(
				lambda(ab)(
					ab(a)
					)
					)
					}         //> MP  : provingground.HoTT.FuncTerm[provingground.HoTT.Term with provingground
                                                  //| .HoTT.Subs[provingground.HoTT.Term],provingground.HoTT.FuncTerm[provinggroun
                                                  //| d.HoTT.FuncTerm[provingground.HoTT.Term,provingground.HoTT.Term] with provin
                                                  //| gground.HoTT.Subs[provingground.HoTT.FuncTerm[provingground.HoTT.Term,provin
                                                  //| gground.HoTT.Term]],provingground.HoTT.Term]] = ((a : A)⟼((a->b : (A⟶B))
                                                  //| ⟼((a->b : (A⟶B))((a : A)) : B)
	MP.typ                                    //> res6: provingground.HoTT.Typ[provingground.HoTT.Term] = (A⟶((A⟶B)⟶B))

	val X = "X" :: __                         //> X  : provingground.HoTT.Typ[provingground.HoTT.Term] with provingground.HoTT
                                                  //| .Subs[provingground.HoTT.Typ[provingground.HoTT.Term]] = X
	val Y = "Y" :: __                         //> Y  : provingground.HoTT.Typ[provingground.HoTT.Term] with provingground.HoTT
                                                  //| .Subs[provingground.HoTT.Typ[provingground.HoTT.Term]] = Y
    
    
    
  MP.subs(A, X)                                   //> res7: provingground.HoTT.FuncTerm[provingground.HoTT.Term with provingground
                                                  //| .HoTT.Subs[provingground.HoTT.Term],provingground.HoTT.FuncTerm[provinggroun
                                                  //| d.HoTT.FuncTerm[provingground.HoTT.Term,provingground.HoTT.Term] with provin
                                                  //| gground.HoTT.Subs[provingground.HoTT.FuncTerm[provingground.HoTT.Term,provin
                                                  //| gground.HoTT.Term]],provingground.HoTT.Term]] = ((a : X)⟼((a->b : (X⟶B))
                                                  //| ⟼((a->b : (A⟶B))((a : A)) : B)
 
 
 A.subs(A, X)                                     //> res8: provingground.HoTT.Typ[provingground.HoTT.Term] = X
 
 val C = "C" :: __                                //> C  : provingground.HoTT.Typ[provingground.HoTT.Term] with provingground.HoTT
                                                  //| .Subs[provingground.HoTT.Typ[provingground.HoTT.Term]] = C
 
 (A ->: C).subs(A, X)                             //> res9: provingground.HoTT.FuncTyp[provingground.HoTT.Term,provingground.HoTT.
                                                  //| Term] = (X⟶C)
 
 (A ->: C).subs(C, X)                             //> res10: provingground.HoTT.FuncTyp[provingground.HoTT.Term,provingground.HoTT
                                                  //| .Term] = (A⟶X)
  C.subs(A, X)                                    //> res11: provingground.HoTT.Typ[provingground.HoTT.Term] = C
  
  
  
  
  
  val ac = "a->c" :: (A ->: C)                    //> ac  : provingground.HoTT.FuncTerm[provingground.HoTT.Term,provingground.HoT
                                                  //| T.Term] with provingground.HoTT.Subs[provingground.HoTT.FuncTerm[provinggro
                                                  //| und.HoTT.Term,provingground.HoTT.Term]] = (a->c : (A⟶C))
  ac.subs(C, X)                                   //> res12: provingground.HoTT.FuncTerm[provingground.HoTT.Term,provingground.Ho
                                                  //| TT.Term] = (a->c : (A⟶X))
  val c = "c" :: C                                //> c  : provingground.HoTT.Term with provingground.HoTT.Subs[provingground.HoT
                                                  //| T.Term] = (c : C)
  
  ac(a).subs(a, c)                                //> res13: provingground.HoTT.Term = ((a->c : (A⟶C))((a : A)) : C)
  val split = applptnterm.unapply(ac(a))          //> split  : Option[(provingground.HoTT.FuncTermLike, provingground.HoTT.Term)]
                                                  //|  = None
  val argopt = split map (_._2)                   //> argopt  : Option[provingground.HoTT.Term] = None
  
  
  argopt map (_.subs(a, c))                       //> res14: Option[provingground.HoTT.Term] = None
  
  a.subs(a, c)                                    //> res15: provingground.HoTT.Term = (c : C)
  
	val x = "x" :: X                          //> x  : provingground.HoTT.Term with provingground.HoTT.Subs[provingground.HoT
                                                  //| T.Term] = (x : X)
	val xy = "x->y" :: (X ->: Y)              //> xy  : provingground.HoTT.FuncTerm[provingground.HoTT.Term,provingground.HoT
                                                  //| T.Term] with provingground.HoTT.Subs[provingground.HoTT.FuncTerm[provinggro
                                                  //| und.HoTT.Term,provingground.HoTT.Term]] = (x->y : (X⟶Y))
	x.typ                                     //> res16: provingground.HoTT.Typ[provingground.HoTT.Term] = X
	xy.typ                                    //> res17: provingground.HoTT.Typ[provingground.HoTT.Term] = (X⟶Y)
	xy(x).typ                                 //> res18: provingground.HoTT.Typ[provingground.HoTT.Term] = Y
	
	MPall                                     //> res19: provingground.HoTT.FuncTerm[provingground.HoTT.Typ[provingground.HoT
                                                  //| T.Term] with provingground.HoTT.Subs[provingground.HoTT.Typ[provingground.H
                                                  //| oTT.Term]],provingground.HoTT.FuncTerm[provingground.HoTT.Typ[provingground
                                                  //| .HoTT.Term] with provingground.HoTT.Subs[provingground.HoTT.Typ[provinggrou
                                                  //| nd.HoTT.Term]],provingground.HoTT.FuncTerm[provingground.HoTT.Term with pro
                                                  //| vingground.HoTT.Subs[provingground.HoTT.Term],provingground.HoTT.FuncTerm[p
                                                  //| rovingground.HoTT.FuncTerm[provingground.HoTT.Term,provingground.HoTT.Term]
                                                  //|  with provingground.HoTT.Subs[provingground.HoTT.FuncTerm[provingground.HoT
                                                  //| T.Term,provingground.HoTT.Term]],provingground.HoTT.Term]]]] = (A⟼(B⟼((
                                                  //| a : A)⟼((a->b : (A⟶B))⟼((a->b : (A⟶B))((a : A)) : B)
	
	MPall.typ                                 //> res20: provingground.HoTT.Typ[provingground.HoTT.Term] = Pi((A⟼Pi((B⟼(A
                                                  //| ⟶((A⟶B)⟶B))))
	
	__.subs(A, X)                             //> res21: provingground.HoTT.Universe = _
	
	val lm = MPall.asInstanceOf[Lambda[Term, Term]]
                                                  //> lm  : provingground.HoTT.Lambda[provingground.HoTT.Term,provingground.HoTT.
                                                  //| Term] = (A⟼(B⟼((a : A)⟼((a->b : (A⟶B))⟼((a->b : (A⟶B))((a : A))
                                                  //|  : B)
	
	val v = lm.value                          //> v  : provingground.HoTT.Term = (B⟼((a : A)⟼((a->b : (A⟶B))⟼((a->b :
                                                  //|  (A⟶B))((a : A)) : B)
	
	val lv = v.asInstanceOf[Lambda[Term, Term]]
                                                  //> lv  : provingground.HoTT.Lambda[provingground.HoTT.Term,provingground.HoTT.
                                                  //| Term] = (B⟼((a : A)⟼((a->b : (A⟶B))⟼((a->b : (A⟶B))((a : A)) : B)
                                                  //| 
	
	lv.variable                               //> res22: provingground.HoTT.Term = B
	
	lv.variable.typ                           //> res23: provingground.HoTT.Typ[provingground.HoTT.Term] = _
	
	lv.variable.subs(A, X)                    //> res24: provingground.HoTT.Term = B
	
	val funny = lv.subs(A, X)                 //> funny  : provingground.HoTT.FuncTerm[provingground.HoTT.Term,provingground.
                                                  //| HoTT.Term] = (B⟼((a : X)⟼((a->b : (X⟶B))⟼((a->b : (A⟶B))((a : A))
                                                  //|  : B)
	
	val bizarre = funny.asInstanceOf[Lambda[Term, Term]]
                                                  //> bizarre  : provingground.HoTT.Lambda[provingground.HoTT.Term,provingground.
                                                  //| HoTT.Term] = (B⟼((a : X)⟼((a->b : (X⟶B))⟼((a->b : (A⟶B))((a : A))
                                                  //|  : B)
	
	bizarre.variable                          //> res25: provingground.HoTT.Term = B
	
	val fa= bizarre.value.asInstanceOf[Lambda[Term, Term]].value.asInstanceOf[Lambda[Term, Term]].value
                                                  //> fa  : provingground.HoTT.Term = ((a->b : (A⟶B))((a : A)) : B)
	applptnterm.unapply(fa)                   //> res26: Option[(provingground.HoTT.FuncTermLike, provingground.HoTT.Term)] =
                                                  //|  None
	
	val inner = applptnterm.unapply(fa).get._2//> java.util.NoSuchElementException: None.get
                                                  //| 	at scala.None$.get(Option.scala:313)
                                                  //| 	at scala.None$.get(Option.scala:311)
                                                  //| 	at src.main.scala.HoTTExperiment$$anonfun$main$1.apply$mcV$sp(src.main.s
                                                  //| cala.HoTTExperiment.scala:124)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execute(Wor
                                                  //| ksheetSupport.scala:75)
                                                  //| 	at src.main.scala.HoTTExperiment$.main(src.main.scala.HoTTExperiment.sca
                                                  //| la:5)
                                                  //| 	at src.main.scala.HoTTExperiment.main(src.main.scala.HoTTExperiment.scal
                                                  //| a)
	
	inner.subs(A, X)
	
	inner.typ == A
	
	inner.asInstanceOf[Symbolic].name
	 
	
	v.subs(A, X)
	
	MPall(X)
	
	MPall(X).typ
	
	MPall(X)(Y)
	
	MPall(X)(Y).typ
	 
	
	MPall.typ
	
	A ~>: (A ->: A)
  (A ~>: (A ->: A)).subs(A, C)

 

 val W : PolyPtn[Term] = IdW
 
  object TestTyp extends SmallTyp{
    override def toString ="implicitType"}
  
  /*
  implicit val aType : Typ[Term] = TestTyp
 
 val cn = "hello" ::: W
 cn.pattern(aType)
 cn.cons
 */
 
  object BoolType extends InductiveTyp with SmallTyp{
  		   
        lazy val constructors = List(this.constructor(this, "true"), cnstr(this))
  }
  BoolType.constructors
 
 
 object NatTyp extends InductiveTyp with SmallTyp{
  //                     implicit val self = this
 
           lazy val constructors =List(cnstr(this), cnstr(this -->: this))
 }
 
 val List(zero, succ) = NatTyp.constructors
 succ.cons.typ
}