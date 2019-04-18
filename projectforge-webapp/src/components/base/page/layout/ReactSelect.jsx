import React from 'react';
import Select from 'react-select';
import makeAnimated from 'react-select/lib/animated';
import PropTypes from "prop-types";
import {dataPropType} from "../../../../utilities/propTypes";
import style from "../../../design/input/Input.module.scss";
import AdditionalLabel from "../../../design/input/AdditionalLabel";

class ReactSelect extends React.Component {
    constructor(props) {
        super(props);

        this.setSelected = this.setSelected.bind(this);
    }

    setSelected(newValue) {
        const {id, changeDataField} = this.props;
        changeDataField(id, newValue);
    }

    render() {
        const {
            label,
            additionalLabel,
            id,
            values,
            data,
        } = this.props;
        let defaultValue = data[id];
        if (defaultValue && typeof data[id] !== 'object')
            defaultValue = {
                value: data[id],
                label: data[id],
            };
        return (<React.Fragment>
                <span className={style.text}>{label}</span>
                <Select
                    //closeMenuOnSelect={false}
                    components={makeAnimated()}
                    defaultValue={defaultValue}
                    isMulti={this.props.isMulti}
                    options={values}
                    isClearable={!this.props.isRequired}
                    getOptionValue={(option) => (option[this.props.valueProperty])}
                    getOptionLabel={(option) => (option[this.props.labelProperty])}
                    onChange={this.setSelected}
                    placeholder={this.props.translations['select.placeholder']}
                />
                <AdditionalLabel title={additionalLabel}/>
            </React.Fragment>
        );
    }
}

ReactSelect.propTypes = {
    changeDataField: PropTypes.func.isRequired,
    data: dataPropType.isRequired,
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    values: PropTypes.arrayOf(PropTypes.object).isRequired,
    valueProperty: PropTypes.string,
    labelProperty: PropTypes.string,
    isMulti: PropTypes.bool,
    isRequired: PropTypes.bool,
};

ReactSelect.defaultProps = {
    valueProperty: 'value',
    labelProperty: 'label',
    isMulti: false,
    isRequired: false,
};
export default ReactSelect;
