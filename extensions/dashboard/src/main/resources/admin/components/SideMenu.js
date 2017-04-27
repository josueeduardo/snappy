import React from "react";

export default class SideMenu extends React.Component {
    render() {
        return (
            <div>
                <div class="sidebar-background">
                    <div class="primary-sidebar-background"></div>
                </div>

                <div class="primary-sidebar">
                    <ul class="nav navbar-collapse collapse navbar-collapse-primary">


                        <li class="active">
                            <span class="glow"></span>
                            <a href="dashboard.html">
                                <i class="icon-dashboard icon-2x"></i>
                                <span>Dashboard</span>
                            </a>
                        </li>


                        <li class="dark-nav ">

                            <span class="glow"></span>


                            <a class="accordion-toggle collapsed " data-toggle="collapse" href="#2rpZ8NEzZU">
                                <i class="icon-beaker icon-2x"></i>
                                <span>UI Lab<i class="icon-caret-down"></i></span>
                            </a>

                            <ul id="2rpZ8NEzZU" class="collapse ">

                                <li class="">
                                    <a href="../ui_lab/buttons.html">
                                        <i class="icon-hand-up"></i> Buttons
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../ui_lab/general.html">
                                        <i class="icon-beaker"></i> General elements
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../ui_lab/icons.html">
                                        <i class="icon-info-sign"></i> Icons
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../ui_lab/grid.html">
                                        <i class="icon-th-large"></i> Grid
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../ui_lab/tables.html">
                                        <i class="icon-table"></i> Tables
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../ui_lab/widgets.html">
                                        <i class="icon-plus-sign-alt"></i> Widgets
                                    </a>
                                </li>

                            </ul>

                        </li>


                        <li class="">
                            <span class="glow"></span>
                            <a href="../forms/forms.html">
                                <i class="icon-edit icon-2x"></i>
                                <span>Forms</span>
                            </a>
                        </li>


                        <li class="">
                            <span class="glow"></span>
                            <a href="../charts/charts.html">
                                <i class="icon-bar-chart icon-2x"></i>
                                <span>Charts</span>
                            </a>
                        </li>


                        <li class="dark-nav ">

                            <span class="glow"></span>


                            <a class="accordion-toggle collapsed " data-toggle="collapse" href="#FpKv3zUmen">
                                <i class="icon-link icon-2x"></i>
                                <span>
                      Others
                      <i class="icon-caret-down"></i>
                    </span>

                            </a>

                            <ul id="FpKv3zUmen" class="collapse ">

                                <li class="">
                                    <a href="../other/wizard.html">
                                        <i class="icon-magic"></i> Wizard
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../other/login.html">
                                        <i class="icon-user"></i> Login Page
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../other/sign_up.html">
                                        <i class="icon-user"></i> Sign Up Page
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../other/full_calendar.html">
                                        <i class="icon-calendar"></i> Full Calendar
                                    </a>
                                </li>

                                <li class="">
                                    <a href="../other/error404.html">
                                        <i class="icon-ban-circle"></i> Error 404 page
                                    </a>
                                </li>

                            </ul>

                        </li>


                    </ul>

                    <div class="hidden-sm hidden-xs">
                        <div class="text-center" style={{marginTop: '60px'}}>
                            <div class="easy-pie-chart-percent" style={{display: 'inlineBlock'}} data-percent="89"><span>89%</span>
                            </div>
                            <div style={{paddingTop: '20px'}}><b>CPU Usage</b></div>
                        </div>

                        <hr class="divider" style={{marginTop: '60px'}}/>

                        <div class="sparkline-box side">

                            <div class="sparkline-row">
                                <h4 class="gray"><span>Orders</span> 847</h4>
                                {/*<div class="sparkline big"data-color="gray"><!--26,24,20,7,12,15,7,19,5,19,29,28--></div>*/}
                            </div>

                            <hr class="divider"/>
                            <div class="sparkline-row">
                                <h4 class="dark-green"><span>Income</span> $43.330</h4>
                                {/*<div class="sparkline big" data-color="darkGreen"><!--27,15,14,21,22,29,8,11,7,25,7,27--></div>*/}
                            </div>

                            <hr class="divider"/>
                            <div class="sparkline-row">
                                <h4 class="blue"><span>Reviews</span> 223</h4>
                                {/*<div class="sparkline big" data-color="blue"><!--20,21,26,14,23,22,11,12,13,16,10,12--></div>*/}
                            </div>

                            <hr class="divider"/>
                        </div>
                    </div>


                </div>
            </div>
        )
    }
}
