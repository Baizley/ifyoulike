import React from 'react';
import './IfYouLike.css';
import Recommendation from "./components/Recommendation";

class IfYouLike extends React.Component {

    constructor(props) {
        super(props);
        this.state = {recommendations: [], blank: this.readBlank()}
    }

    readBlank() {
        const pathName = window.location.pathname;
        return pathName.replace('/ifyoulike', '').replace('/index.html', '');
    }

    capitalizeFirstLetter(word) {
        return word.charAt(0).toUpperCase() + word.substring(1);
    }

    componentDidMount() {
        new EventSource(`/ifyoulike${this.state.blank}`).addEventListener(
            "recommendation",
            (event) => this.setState({recommendations: [...this.state.recommendations, JSON.parse(event.data)]})
        );
    }

    render() {
        return (
            <div class="center">
                <h1 id="title">If You Like {this.capitalizeFirstLetter(this.state.blank)}</h1>
                <div class="recommendations">
                    {this.state.recommendations.map((recommendation) => <Recommendation recommendation={recommendation}/>)}
                </div>
            </div>
        );
    }
}

export default IfYouLike;