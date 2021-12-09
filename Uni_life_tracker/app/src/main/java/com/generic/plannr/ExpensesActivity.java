package com.generic.plannr;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.generic.plannr.Entities.Expense;
import com.generic.plannr.Gateways.ExpenseGateway;
import com.generic.plannr.Gateways.UserGateway;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ExpensesActivity extends AppCompatActivity {

    private ArrayList<Expense> expensesList;
    private RecyclerView rvExpenses;
    private DrawerLayout drawerLayout;
    private MainActivity activity;
    private TextInputEditText etIncome;
    private TextInputLayout tiIncome;
    private TextView tvTotalExpenses, tvBalance;
    private double totalExpenses, balance, income;
    UserGateway ug = new UserGateway(ExpensesActivity.this);
    ExpenseGateway eg = new ExpenseGateway(ExpensesActivity.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        activity = new MainActivity();
        expensesList = new ArrayList<>();
        rvExpenses = findViewById(R.id.rv_expenses); // expense list
        drawerLayout = findViewById(R.id.drawer_layout); // nav menu
        etIncome = findViewById(R.id.et_income);
        tiIncome = findViewById(R.id.ti_income);
        tvTotalExpenses = findViewById(R.id.tv_total_expenses);
        tvBalance = findViewById(R.id.tv_balance);

        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        boolean firstStart = preferences.getBoolean("firstStart", true);

        etIncome.setText(ug.getLoggedInIncome());
        calculateExpense();

        income = Double.parseDouble(Objects.requireNonNull(tiIncome.getEditText()).getText().toString());
        updateBalance();

        if (firstStart) {
            showTargetView();
        }

        // Uses the recycler view to display the event list
        setExpenseInfo();
        setAdapter();
    }

    /**
     * Sets up the recycler view for expenses list.
     */
    private void setAdapter() {
        ListExpenses adapter = new ListExpenses(expensesList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rvExpenses.setLayoutManager(layoutManager);
        rvExpenses.setItemAnimator(new DefaultItemAnimator());
        rvExpenses.setAdapter(adapter);
    }

    /**
     * Displays target view upon first launch. Target view prompts user to add expense.
     */
    private void showTargetView() {
        TapTargetView.showFor(this, TapTarget.forView(
                                findViewById(R.id.iv_add_expense), "Add an Expense", "Add your expenses here!")
                        .outerCircleColor(R.color.lavender).targetCircleColor(R.color.white)
                        .titleTextColor(R.color.black).descriptionTextColor(R.color.black)
                        .tintTarget(false)
                        .cancelable(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        clickAddExpense(view);
                    }
                });

        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }

    /**
     * Creates an instance of expense database and an instance of userinfo database
     * and adds all expenses to expense list
     */
    private void setExpenseInfo() {
        expensesList.addAll(eg.getAllExpenses(ug.getLoggedInUserID()));
    }

    /**
     * Calculates the total expenses using the expense list
     */
    public void calculateExpense() {
        totalExpenses = 0.00;
        for (Expense e : eg.getAllExpenses(ug.getLoggedInUserID()))
            totalExpenses += e.getValue();

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        tvTotalExpenses.setText(formatter.format(totalExpenses));

    }

    /**
     * Prompts the Add Expense Activity on a plus icon click.
     *
     * @param view a View for the device screen.
     */
    public void clickAddExpense(View view) {
        // clicking the check in order to add expense
        activity.redirectActivity(this, AddExpensesActivity.class);
    }

    /**
     * Checks if the income is greater or less than the total
     * and changes colour of the text accordingly
     */
    public void clickSaveIncome(View view) {
        income = Double.parseDouble(Objects.requireNonNull(tiIncome.getEditText()).getText().toString());
        ug.updateIncome(income);
        //ug.updateIncome(income);
        updateBalance();
    }

    public void updateBalance() {
        balance = income - totalExpenses;
        if (balance > 0) {
            tvBalance.setTextColor(Color.GREEN);
        } else if (balance < 0) {
            tvBalance.setTextColor(Color.RED);
        } else {
            tvBalance.setTextColor(Color.BLACK);
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        tvBalance.setText(formatter.format(balance));
    }

    /**
     * Opens navigation menu on menu icon click.
     *
     * @param view a View for the device screen.
     */
    public void clickMenu(View view) {
        activity.openDrawer(drawerLayout);
    } // open drawer

    /**
     * Directs activity to the Main activity on logo click.
     *
     * @param view a View for the device screen.
     */
    public void clickLogo(View view) {
        activity.redirectActivity(this, MainActivity.class);
    } // redirect activity to main

    /**
     * Directs activity to the School activity on school icon click.
     *
     * @param view a View for the device screen.
     */
    public void clickSchool(View view) {
        activity.redirectActivity(this, SchoolActivity.class);
    } // redirect activity to school

    // TODO: change this to life later
//    public void clickLife(View view) { activity.redirectActivity(this, MainActivity.class); } // redirect activity to life

    /**
     * Directs activity to the Expenses activity on expenses icon click.
     *
     * @param view a View for the device screen.
     */
    public void clickExpenses(View view) {
    } // recreate activity

    /**
     * Directs activity to the Settings activity on settings icon click.
     *
     * @param view a View for the device screen.
     */
    public void clickSettings(View view) {
        activity.redirectActivity(this, SettingsActivity.class);
    } // redirect activity to settings

    /**
     * Prompts log out on a logout icon click.
     *
     * @param view a View for the device screen.
     */
    public void clickLogOut(View view) {
        activity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activity.closeDrawer(drawerLayout); // close drawer
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}