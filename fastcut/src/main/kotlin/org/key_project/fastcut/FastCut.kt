package org.key_project.fastcut

import de.uka.ilkd.key.control.InstantiationFileHandler
import de.uka.ilkd.key.core.KeYMediator
import de.uka.ilkd.key.gui.MainWindow
import de.uka.ilkd.key.gui.actions.KeyAction
import de.uka.ilkd.key.gui.extension.api.ContextMenuKind
import de.uka.ilkd.key.gui.extension.api.DefaultContextMenuKind
import de.uka.ilkd.key.gui.extension.api.KeYGuiExtension
import de.uka.ilkd.key.gui.nodeviews.CurrentGoalViewMenu
import de.uka.ilkd.key.logic.Name
import de.uka.ilkd.key.logic.Term
import de.uka.ilkd.key.nparser.KeyIO
import de.uka.ilkd.key.pp.LogicPrinter
import de.uka.ilkd.key.proof.Goal
import de.uka.ilkd.key.proof.Proof
import de.uka.ilkd.key.rule.NoPosTacletApp
import de.uka.ilkd.key.rule.TacletApp
import de.uka.ilkd.key.rule.inst.TermInstantiation
import de.uka.ilkd.key.util.parsing.BuildingException
import de.uka.ilkd.key.util.parsing.SyntaxErrorReporter
import java.awt.event.ActionEvent
import java.util.*
import java.util.stream.Stream
import javax.swing.Action

/**
 * @author Alexander Weigl
 * @version 1 (11/13/21)
 */
@KeYGuiExtension.Info(
    name = "Explorative Sideproof Rules",
    optional = true,
    experimental = false,
    description = "Allows to do stuff...",
    priority = 10000
)
class FastCut  //public static final File cutFile = getFastCutFile();
/**
 * Environment variable or java system property where the cuts definition can be found.
 * public static final String FAST_CUT_FILE = "FAST_CUT_FILE";
 *
 * @Nonnull private static File getFastCutFile() {
 * File file = new File(PathConfig.getKeyConfigDir(), "fastcuts.txt");
 * if (System.getProperty(FAST_CUT_FILE) != null) {
 * file = new File(System.getProperty(FAST_CUT_FILE));
 * }
 * if (System.getenv(FAST_CUT_FILE) != null) {
 * file = new File(System.getProperty(FAST_CUT_FILE));
 * }
 * return file;
 * }
 */
//    public final List<String> cutSnippets = new ArrayList<>();
    : KeYGuiExtension, KeYGuiExtension.ContextMenu {
    override fun getContextActions(mediator: KeYMediator, kind: ContextMenuKind, underlyingObject: Any): List<Action> {
        if (kind === DefaultContextMenuKind.GOAL_VIEW) {
            val ui = MainWindow.getInstance().userInterface
            val proof = mediator.selectedProof
            val goal = mediator.selectedGoal
            if (goal != null) {
                val seq = ArrayList<Action>(64)
                seq.addAll(getFastCuts(mediator, underlyingObject as CurrentGoalViewMenu.GoalViewData, proof))
                seq.addAll(getFastInstantiations(mediator, underlyingObject as CurrentGoalViewMenu.GoalViewData, proof))
                seq.add(GatherCutsAndStoreAction(mediator.selectedProof))
                return seq
            }
        }
        return emptyList()
    }

    private fun getFastInstantiations(
        mediator: KeYMediator,
        underlyingObject: CurrentGoalViewMenu.GoalViewData,
        proof: Proof
    ): Collection<Action> {
/*        var allRight = InstantiationFileHandler.getInstantiationListsFor(ALLRIGHT_TACLET_NAME.toString())
                .stream().map(it -> it.get(0));
        var exLeft = InstantiationFileHandler.getInstantiationListsFor(ALLRIGHT_TACLET_NAME.toString())
                .stream().map(it -> it.get(0));

        var kio = new KeyIO(proof.getServices());

        return Stream.concat(
                        create(allRight, kio, ApplyFastAllRight::new, mediator, underlyingObject),
                        create(exLeft, kio, ApplyExLeftRight::new, mediator, underlyingObject))
                .collect(Collectors.toList());

 */
        return emptyList()
    }

    private fun create(
        instantiations: Stream<String>, kio: KeyIO, constr: CreateAction,
        mediator: KeYMediator, underlyingObject: CurrentGoalViewMenu.GoalViewData
    ): Stream<Action?> {
        return instantiations.map { it: String? ->
            val term: Term?
            term = try {
                kio.parseExpression(it!!)
            } catch (e: SyntaxErrorReporter.ParserException) {
                null
            } catch (e: BuildingException) {
                null
            }
            constr.create(it, term, mediator, underlyingObject)
        }
    }

    private fun getFastCuts(
        mediator: KeYMediator, underlyingObject: CurrentGoalViewMenu.GoalViewData,
        proof: Proof
    ): ArrayList<Action> {
        val lines = InstantiationFileHandler.getInstantiationListsFor(CUT_TACLET_NAME.toString())
            .stream().map { it: List<String?> -> it[0] }
        val kio = KeyIO(proof.services)
        val seq = ArrayList<Action>() //InstantiationFileHandler.SAVE_COUNT);
        lines.forEach { it: String? ->
            val term: Term?
            term = try {
                kio.parseExpression(it!!)
            } catch (e: SyntaxErrorReporter.ParserException) {
                null
            } catch (e: BuildingException) {
                null
            }
            val action = ApplyFastCutAction(
                it, term, mediator,
                underlyingObject
            )
            seq.add(action)
        }
        return seq
    }

    class ApplyFastCutAction(
        line: String?,
        term: Term?, mediator: KeYMediator,
        underlyingObject: CurrentGoalViewMenu.GoalViewData?
    ) : KeyAction() {
        private val proof: Proof
        private val term: Term?
        private val goal: Goal?

        init {
            menuPath = "Fast Cuts"
            name = line
            this.term = term
            proof = mediator.selectedProof
            goal = mediator.selectedGoal
            isEnabled = term != null && goal != null
        }

        override fun actionPerformed(e: ActionEvent) {
            val cut = proof.env.initConfigForEnvironment
                .lookupActiveTaclet(CUT_TACLET_NAME)
            var app: TacletApp = NoPosTacletApp.createNoPosTacletApp(cut)
            val cutFormula = app.uninstantiatedVars().iterator().next()
            app = app.addCheckedInstantiation(cutFormula, term, proof.services, true)
            goal!!.apply(app)
        }
    }

    inner class GatherCutsAndStoreAction(proof: Proof) : KeyAction() {
        private val proof: Proof

        init {
            name = "Gather cut terms and store them"
            menuPath = "Fast Cuts"
            this.proof = proof
        }

        override fun actionPerformed(e: ActionEvent) {
            /*try {
                var gathered = gatherCuts();
                //cutSnippets.addAll(gathered);
                //Files.createFile(cutFile.toPath());
                //Files.write(cutFile.toPath(), cutSnippets);
            } catch (IOException ex) {
                ExceptionDialog.showDialog(MainWindow.getInstance(), ex);
            }*/
        }

        private fun gatherCuts(): List<String> {
            val iter = proof.root().subtreeIterator()
            val terms = LinkedList<String>()
            while (iter != null && iter.hasNext()) {
                val n = iter.next()
                val app = n.appliedRuleApp ?: continue
                val rule = app.rule()
                if (rule.name() == CUT_TACLET_NAME) {
                    val tapp = n.appliedRuleApp as NoPosTacletApp
                    val instantiation = tapp.matchConditions().instantiations
                        .lookupEntryForSV(Name("cutFormula")).value() as TermInstantiation
                    val cutFormula = instantiation.instantiation
                    val repr = LogicPrinter.quickPrintTerm(cutFormula, proof.services)
                    println("cutFormula found: $repr")
                    terms.add(repr)
                }
            }
            return terms
        }
    }

    private inner class ApplyFastAllRight(
        s: String?,
        term: Term?,
        keYMediator: KeYMediator?,
        goalViewData: CurrentGoalViewMenu.GoalViewData?
    ) : KeyAction() {
        override fun actionPerformed(e: ActionEvent) {}
    }

    private inner class ApplyExLeftRight(
        s: String?,
        term: Term?,
        keYMediator: KeYMediator?,
        goalViewData: CurrentGoalViewMenu.GoalViewData?
    ) : KeyAction() {
        override fun actionPerformed(e: ActionEvent) {}
    }

    private interface CreateAction {
        fun create(
            it: String?,
            term: Term?,
            mediator: KeYMediator?,
            underlyingObject: CurrentGoalViewMenu.GoalViewData?
        ): Action?
    }
}

private val CUT_TACLET_NAME = Name("cut")
private val ALLRIGHT_TACLET_NAME = Name("allRight")
private val EXLEFT_TACLET_NAME = Name("exLeft")