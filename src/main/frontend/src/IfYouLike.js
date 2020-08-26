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

    capitalizeFirstLetters(words) {
        return words.split(' ').map(word => word.charAt(0).toUpperCase() + word.substring(1)).join(" ");
    }

    sortRecommendations(recommendations) {
        return recommendations.sort((r1, r2) => r2.score - r1.score);
    }

    componentDidMount() {
        document.title = "If You Like " + this.capitalizeFirstLetters(decodeURIComponent(this.state.blank));
        const eventSource = new EventSource(`/ifyoulike${this.state.blank}`);
        eventSource.addEventListener(
            "recommendation",
            (event) => {
                setTimeout(
                    () => this.setState({recommendations: this.sortRecommendations([...this.state.recommendations, JSON.parse(event.data)])}),
                    this.state.recommendations.length
                )

            }
        );
        eventSource.addEventListener(
            "COMPLETE",
            (event) => eventSource.close()
        )
    }

    render() {
        return (
            <div class="center">
                <div id="title">If you like <div id="blank">{this.capitalizeFirstLetters(decodeURIComponent(this.state.blank))}</div> then you might like</div>
                <div class="recommendations">
                    {this.state.recommendations.map((recommendation, index) => <Recommendation key={index} recommendation={recommendation}/>)}
                </div>
            </div>
        );
    }
}

export default IfYouLike;