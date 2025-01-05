import { useEffect, useState } from "react";
import ReactDOM from "react-dom";
import ModalProps from "./ModalProps";

const DeleteFoodModal: React.FC<ModalProps>  = ({isOpen, children})=>{
   let [show, setShow] = useState(false);

    useEffect(() => {
        setShow(isOpen);
    }, [isOpen]);

    let toggleVisibility = () => {
        setShow(!show);
    }

    useEffect(() => {
        if (!show) {
            setShow(false)
        }
    }, [show]);


    return <div className="modal-scratchpad">
        {show ?
            ReactDOM.createPortal(
                <div className="sample-modal-wrapper">
                    <div className="sample-modal-backdrop">
                    </div>
                    <div className="sample-modal-container">
                        <div className="sample-modal">
                            <div onClick={toggleVisibility} className="sample-modal-cross-button">{'\u2716'}</div>
                        </div>
                        <style type="text/css">
                            {"body {" +
                            "overflow:hidden" +
                            "}"}
                        </style>
                    </div>
                </div>
                , document.body)
            : <></>
        }
    </div>

};

export default DeleteFoodModal;