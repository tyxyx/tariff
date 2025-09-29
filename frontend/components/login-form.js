import React, { Children } from "react";
import Form from 'next/form'
import { colors } from "@/styles/colors";
import { Button } from "@/components/ui/button";


export function LoginForm({ children, className = "" }){
    return (
        <div>
            <Form action="/">
              {/* On submission, it will redirect to to-do page*/}
                  <div className={`space-y-6 ${className}`}>{children}</div>
              <Button className="mt-6" type="submit">Login</Button>
            </Form>
        </div>
    )
}

export function LoginInput({children, className = "" }){
    return (
        <div className={` flex flex-col ${className}`}>
            <label className="mb-2">{children}</label>
            <input className="p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:outline-none"
                    type="text" />
        </div>
    );  
};