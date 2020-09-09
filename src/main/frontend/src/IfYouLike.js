import React from 'react';
import './IfYouLike.css';
import 'font-awesome/css/font-awesome.min.css';
import Recommendation from "./components/Recommendation";
import Container from '@material-ui/core/Container';

class IfYouLike extends React.Component {

    constructor(props) {
        super(props);
        this.recommendationsBuffer = [];
        this.state = {recommendations: [], blank: this.readBlank()}
        this.intervalId = setInterval(
            () => this.renderBuffer(),
            100
        );
    }

    renderBuffer() {
        this.setState({recommendations: this.sortRecommendations([...this.state.recommendations, ...this.recommendationsBuffer])})
        this.recommendationsBuffer = [];
    }

    readBlank() {
        const pathName = window.location.pathname;
        return pathName.replace('/ifyoulike', '').replace('/index.html', '');
    }

    capitalizeFirstLetters(words) {
        return words.split(' ').map(word => word.charAt(0).toUpperCase() + word.substring(1)).join(' ');
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
                const recommendation = JSON.parse(event.data);
                this.recommendationsBuffer.push(recommendation);
                this.sortRecommendations(this.recommendationsBuffer);
            }
        );
        eventSource.addEventListener(
            "COMPLETE",
            (event) => {
                eventSource.close();
                clearInterval(this.intervalId);
                this.renderBuffer();
            }
        )
    }

    render() {
        return (
            <Container maxWidth="sm">
                <div id="title">If you like <div
                    id="blank">{this.capitalizeFirstLetters(decodeURIComponent(this.state.blank))}</div> then you might
                    like
                </div>
                <div className="recommendations">
                    {this.state.recommendations.map((recommendation, index) => <Recommendation key={index}
                                                                                               recommendation={recommendation}/>)}
                </div>
            </Container>
        );
    }
}

export default IfYouLike;